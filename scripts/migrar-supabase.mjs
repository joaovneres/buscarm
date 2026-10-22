#!/usr/bin/env node
/**
 * Migração de dados de um projeto Supabase (sistema-alunos) para o BuscaRM.
 *
 * Lê a tabela de alunos via API REST do Supabase (PostgREST) e gera DOIS artefatos:
 *   1. supabase-registros.csv -> entra pela tela /importacao (passa por conferência humana);
 *   2. backup.sql             -> INSERTs diretos em registro_matricula (carga de uma vez).
 *
 * As regras de derivação de 'sobrenome' e 'nome_busca' replicam NomeNormalizer.java, para que os
 * dados migrados fiquem indistinguíveis dos cadastrados pelo próprio sistema.
 *
 * Uso:
 *   SUPABASE_URL=https://xxxx.supabase.co SUPABASE_KEY=sb_publishable_... \
 *     node scripts/migrar-supabase.mjs [tabela]
 *
 * Sem o argumento [tabela], o script apenas lista as tabelas expostas e encerra.
 */

import fs from 'node:fs/promises';
import path from 'node:path';

const URL_BASE = (process.env.SUPABASE_URL || '').replace(/\/+$/, '');
const CHAVE = process.env.SUPABASE_KEY || '';
const TABELA = process.argv[2];
const DIR_SAIDA = process.env.OUT_DIR || 'migracao';
const USUARIO_AUDITORIA = process.env.USUARIO_MIGRACAO || 'migracao-supabase';
/** Importação em lote nasce PENDENTE (StatusValidacao); use VALIDADO se os dados já foram conferidos. */
const STATUS = process.env.STATUS_MIGRACAO || 'PENDENTE';
/** 'title' converte nomes em CAIXA ALTA para Title Case; 'original' grava a caixa como veio. */
const CAIXA_NOME = process.env.CAIXA_NOME || 'title';
const PAGINA = 1000;

/** Caminho de um CSV já baixado: regera os artefatos sem tocar no Supabase. */
const ORIGEM_CSV = process.env.ORIGEM_CSV || '';

if (!ORIGEM_CSV && (!URL_BASE || !CHAVE)) {
    console.error('Defina SUPABASE_URL e SUPABASE_KEY (ou ORIGEM_CSV para regerar de um CSV local).');
    process.exit(1);
}

const cabecalhos = {
    apikey: CHAVE,
    Authorization: `Bearer ${CHAVE}`,
    Accept: 'application/json',
};

// --- regras de domínio (espelham NomeNormalizer.java) ---

/** minúsculas, sem acentos, espaços colapsados — base da busca por nome. */
function normalizar(texto) {
    if (texto == null) {
        return '';
    }
    return String(texto)
        .normalize('NFD')
        .replace(/\p{M}+/gu, '')
        .trim()
        .toLowerCase()
        .replace(/\s+/g, ' ');
}

/**
 * Conectivos que ficam em minúscula no meio do nome ("Maria da Silva"), nunca na primeira palavra.
 * 'e' entra aqui por causa de "Costa e Silva".
 */
const CONECTIVOS = new Set(['de', 'da', 'do', 'das', 'dos', 'e', 'di', 'du', 'del', 'la', 'das']);

/**
 * Capitaliza a primeira letra e também o que vem depois de apóstrofo ou hífen: d'arc -> D'Arc.
 * O {@code ^\P{L}*} garante que pontuação à esquerda não impeça a capitalização ("(ana" -> "(Ana").
 */
function capitalizarPalavra(palavra) {
    return palavra.replace(
        /(^\P{L}*|['’-])(\p{L})/gu,
        (_, separador, letra) => separador + letra.toUpperCase(),
    );
}

/**
 * CAIXA ALTA da origem -> Title Case para exibição ("ROBERTO GARCIA ZUIN" -> "Roberto Garcia Zuin").
 * Não afeta a busca: 'nome_busca' é sempre normalizado para minúsculas.
 */
function paraTitleCase(nomeCompleto) {
    const palavras = String(nomeCompleto).trim().replace(/\s+/g, ' ').toLowerCase().split(' ');
    return palavras
        .map((palavra, i) => (i > 0 && CONECTIVOS.has(palavra) ? palavra : capitalizarPalavra(palavra)))
        .join(' ');
}

/** "Sobrenome, Nome" -> parte antes da vírgula; nome natural -> última palavra. */
function extrairSobrenome(nomeCompleto) {
    if (!nomeCompleto || !String(nomeCompleto).trim()) {
        return '';
    }
    const nome = String(nomeCompleto).trim().replace(/\s+/g, ' ');
    const virgula = nome.indexOf(',');
    if (virgula >= 0) {
        return nome.slice(0, virgula).trim();
    }
    const partes = nome.split(' ');
    return partes[partes.length - 1];
}

// --- acesso ao Supabase ---

async function requisitar(caminho) {
    const resposta = await fetch(`${URL_BASE}${caminho}`, { headers: cabecalhos });
    if (!resposta.ok) {
        const corpo = (await resposta.text()).slice(0, 300);
        throw new Error(`HTTP ${resposta.status} em ${caminho}: ${corpo}`);
    }
    return resposta.json();
}

/** Nomes tentados quando a introspecção não está disponível (chave publishable). */
const TABELAS_CANDIDATAS = [
    'alunos', 'aluno', 'students', 'student', 'matriculas', 'matricula',
    'registros', 'registro', 'cadastros', 'fichas', 'prontuarios', 'turmas',
];

/**
 * Tabelas/views expostas pelo PostgREST, lidas do documento OpenAPI da raiz.
 *
 * Esse endpoint exige chave <b>secret</b>; com uma chave publishable ele responde 401. Nesse caso
 * caímos para a sondagem de nomes conhecidos (200 = existe e é legível, 404 = não existe), que é o
 * máximo possível sem introspecção.
 */
async function listarTabelas() {
    try {
        const openapi = await requisitar('/rest/v1/');
        const caminhos = Object.keys(openapi.paths || {})
            .filter((p) => p !== '/' && !p.includes('{'))
            .map((p) => p.replace(/^\//, ''))
            .filter((p) => p && !p.startsWith('rpc/'));
        return [...new Set(caminhos)].sort();
    } catch (erro) {
        if (!String(erro.message).includes('HTTP 401')) {
            throw erro;
        }
        console.error('  (introspecção exige chave secret; sondando nomes conhecidos)');
        const encontradas = [];
        for (const tabela of TABELAS_CANDIDATAS) {
            const resposta = await fetch(
                `${URL_BASE}/rest/v1/${tabela}?select=*&limit=1`,
                { headers: cabecalhos },
            );
            if (resposta.ok) {
                encontradas.push(tabela);
            }
        }
        return encontradas;
    }
}

/** Baixa a tabela inteira, paginando. Ordena por uma coluna estável para não repetir linhas. */
async function baixarTabela(tabela) {
    const amostra = await requisitar(`/rest/v1/${tabela}?select=*&limit=1`);
    if (!amostra.length) {
        return [];
    }
    const colunas = Object.keys(amostra[0]);
    const colunaOrdem = ['id', 'uuid', 'rm', 'matricula', 'created_at'].find((c) => colunas.includes(c))
        || colunas[0];

    const linhas = [];
    for (let offset = 0; ; offset += PAGINA) {
        const ordem = encodeURIComponent(colunaOrdem);
        const pagina = await requisitar(
            `/rest/v1/${tabela}?select=*&order=${ordem}.asc&limit=${PAGINA}&offset=${offset}`,
        );
        linhas.push(...pagina);
        process.stderr.write(`\r  baixados ${linhas.length} registros...`);
        if (pagina.length < PAGINA) {
            break;
        }
    }
    process.stderr.write('\n');
    return linhas;
}

// --- origem alternativa: CSV já baixado (regera sem acessar o Supabase de novo) ---

/** Parser RFC 4180 com delimitador ";" — espelha o LeitorPlanilhaAdapter do backend. */
function parseCsv(texto, delimitador = ';') {
    const linhas = [];
    let linha = [];
    let campo = '';
    let dentroAspas = false;
    let comConteudo = false;

    for (let i = 0; i < texto.length; i++) {
        const ch = texto[i];
        if (dentroAspas) {
            if (ch === '"') {
                if (texto[i + 1] === '"') {
                    campo += '"';
                    i++;
                } else {
                    dentroAspas = false;
                }
            } else {
                campo += ch;
            }
            continue;
        }
        if (ch === '"') {
            dentroAspas = true;
            comConteudo = true;
        } else if (ch === delimitador) {
            linha.push(campo.trim());
            campo = '';
            comConteudo = true;
        } else if (ch === '\n' || ch === '\r') {
            if (ch === '\r' && texto[i + 1] === '\n') {
                i++;
            }
            linha.push(campo.trim());
            campo = '';
            if (comConteudo) {
                linhas.push(linha);
            }
            linha = [];
            comConteudo = false;
        } else {
            campo += ch;
            if (!/\s/.test(ch)) {
                comConteudo = true;
            }
        }
    }
    linha.push(campo.trim());
    if (comConteudo) {
        linhas.push(linha);
    }
    return linhas;
}

/** Lê um CSV local e devolve objetos com as chaves do cabeçalho, no mesmo formato do PostgREST. */
async function lerCsvLocal(caminho) {
    const texto = (await fs.readFile(caminho, 'utf8')).replace(/^﻿/, '');
    const linhas = parseCsv(texto);
    if (linhas.length < 2) {
        throw new Error(`CSV sem dados: ${caminho}`);
    }
    const cabecalho = linhas[0];
    return linhas.slice(1).map((campos) => Object.fromEntries(
        cabecalho.map((coluna, i) => [coluna, campos[i] ?? '']),
    ));
}

// --- mapeamento das colunas de origem para os campos do BuscaRM ---

/** Escolhe a coluna de origem cujo nome normalizado casa com um dos candidatos (exato antes de parcial). */
function acharColuna(colunas, candidatos) {
    const normalizadas = colunas.map((c) => ({
        original: c,
        chave: normalizar(c).replace(/[\s_-]/g, ''),
    }));
    for (const candidato of candidatos) {
        const exata = normalizadas.find((c) => c.chave === candidato);
        if (exata) {
            return exata.original;
        }
    }
    for (const candidato of candidatos) {
        const parcial = normalizadas.find((c) => c.chave.includes(candidato));
        if (parcial) {
            return parcial.original;
        }
    }
    return null;
}

function mapear(linhas) {
    const colunas = Object.keys(linhas[0]);
    const colNome = process.env.COL_NOME || acharColuna(colunas, ['nomecompleto', 'nome', 'aluno', 'estudante']);
    const colRm = process.env.COL_RM || acharColuna(colunas, ['rm', 'matricula', 'registro', 'numero']);
    const colAno = process.env.COL_ANO || acharColuna(colunas, ['anoregistro', 'ano', 'anoletivo']);

    if (!colNome || !colRm) {
        throw new Error(
            `Não identifiquei as colunas de nome e RM. Colunas disponíveis: ${colunas.join(', ')}.`
            + ' Informe COL_NOME e COL_RM no ambiente.',
        );
    }
    const rotuloAno = colAno ? `"${colAno}"` : '(ausente)';
    console.error(`  nome <- "${colNome}" | rm <- "${colRm}" | ano <- ${rotuloAno}`);

    const registros = [];
    const descartados = [];
    const rmsVistos = new Map();

    for (const linha of linhas) {
        const bruto = linha[colNome] == null ? '' : String(linha[colNome]).trim().replace(/\s+/g, ' ');
        const nome = CAIXA_NOME === 'title' && bruto ? paraTitleCase(bruto) : bruto;
        const rm = Number.parseInt(String(linha[colRm] ?? '').trim(), 10);
        const ano = colAno ? Number.parseInt(String(linha[colAno] ?? '').trim(), 10) : NaN;

        if (!nome || !Number.isInteger(rm)) {
            descartados.push({ motivo: nome ? 'RM não numérico' : 'nome vazio', linha });
            continue;
        }
        if (rmsVistos.has(rm)) {
            descartados.push({ motivo: `RM ${rm} duplicado (mantido "${rmsVistos.get(rm)}")`, linha });
            continue;
        }
        rmsVistos.set(rm, nome);
        registros.push({
            rm,
            nomeCompleto: nome,
            sobrenome: extrairSobrenome(nome),
            nomeBusca: normalizar(nome),
            anoRegistro: Number.isInteger(ano) ? ano : null,
        });
    }
    registros.sort((a, b) => a.rm - b.rm);
    return { registros, descartados };
}

// --- geração dos artefatos ---

/** Campo CSV conforme RFC 4180 (o LeitorPlanilhaAdapter detecta ";" como delimitador). */
function campoCsv(valor) {
    const texto = valor == null ? '' : String(valor);
    return /[;"\r\n]/.test(texto) ? `"${texto.replace(/"/g, '""')}"` : texto;
}

function gerarCsv(registros) {
    const linhas = ['Nome;RM;Ano'];
    for (const r of registros) {
        linhas.push([campoCsv(r.nomeCompleto), r.rm, r.anoRegistro ?? ''].join(';'));
    }
    return `﻿${linhas.join('\r\n')}\r\n`;
}

const literal = (valor) => `'${String(valor).replace(/'/g, "''")}'`;

function gerarSql(registros, tabela) {
    const valores = registros.map((r) => `  (${r.rm}, ${literal(r.nomeCompleto)}, ${literal(r.sobrenome)}, `
        + `${literal(r.nomeBusca)}, ${r.anoRegistro ?? 'NULL'}, TRUE, ${literal(STATUS)})`);
    return [
        `-- Carga de ${registros.length} registros migrados do Supabase (tabela "${tabela}").`,
        `-- Gerado por scripts/migrar-supabase.mjs em ${new Date().toISOString()}.`,
        '--',
        '-- O trigger trg_registro_auditoria (V2) recusa gravação sem o usuário da sessão, por isso o',
        '-- set_config abaixo é obrigatório: é dele que saem criado_por e atualizado_por.',
        '-- Rodar com: psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f backup.sql',
        '',
        'BEGIN;',
        `SELECT set_config('app.current_username', ${literal(USUARIO_AUDITORIA)}, true);`,
        '',
        'INSERT INTO registro_matricula (rm, nome_completo, sobrenome, nome_busca, ano_registro, ativo, status)',
        'VALUES',
        `${valores.join(',\n')}`,
        'ON CONFLICT (rm) DO NOTHING;',
        '',
        'COMMIT;',
        '',
    ].join('\n');
}

// --- orquestração ---

if (!ORIGEM_CSV && !TABELA) {
    console.error(`Tabelas expostas em ${URL_BASE}:`);
    for (const t of await listarTabelas()) {
        console.error(`  - ${t}`);
    }
    console.error('\nRode de novo passando a tabela de alunos como argumento.');
    process.exit(0);
}

let linhas;
let origem;
if (ORIGEM_CSV) {
    console.error(`Lendo CSV local ${ORIGEM_CSV} ...`);
    linhas = await lerCsvLocal(ORIGEM_CSV);
    origem = ORIGEM_CSV;
} else {
    console.error(`Baixando "${TABELA}" de ${URL_BASE} ...`);
    linhas = await baixarTabela(TABELA);
    origem = TABELA;
}
if (!linhas.length) {
    console.error('A tabela está vazia — nada a migrar.');
    process.exit(1);
}

const { registros, descartados } = mapear(linhas);
await fs.mkdir(DIR_SAIDA, { recursive: true });
const arquivoCsv = path.join(DIR_SAIDA, 'supabase-registros.csv');
const arquivoSql = path.join(DIR_SAIDA, 'backup.sql');
await fs.writeFile(arquivoCsv, gerarCsv(registros), 'utf8');
await fs.writeFile(arquivoSql, gerarSql(registros, origem), 'utf8');

if (descartados.length) {
    const arquivoLog = path.join(DIR_SAIDA, 'descartados.json');
    await fs.writeFile(arquivoLog, JSON.stringify(descartados, null, 2), 'utf8');
    console.error(`\n${descartados.length} linha(s) descartada(s) — detalhes em ${arquivoLog}`);
}
console.error(`\nOK: ${registros.length} registros`);
console.error(`  CSV (tela /importacao): ${arquivoCsv}`);
console.error(`  SQL (carga direta):     ${arquivoSql}  [status=${STATUS}]`);
