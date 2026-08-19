import { expect, test } from '@playwright/test';

/** Sessão fake (o app só lê token + usuário do localStorage, sem validar o JWT). */
const sessao = {
  token: 'token-fake-e2e',
  usuario: { nome: 'Secretaria Teste', username: 'sec', papel: 'SECRETARIA' },
};

test.beforeEach(async ({ page }) => {
  await page.addInitScript((s) => {
    localStorage.setItem('buscarm.token', s.token);
    localStorage.setItem('buscarm.usuario', JSON.stringify(s.usuario));
  }, sessao);

  // Não bate no backend real: a lista volta vazia, o topo (Importar/Exportar) independe disso.
  await page.route('**/api/registros**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ conteudo: [], pagina: 0, tamanho: 20, total: 0, totalPaginas: 0 }),
    });
  });
});

test('não há tela inicial: logado vai direto para /registros', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveURL(/\/registros$/);
  await expect(page.getByRole('heading', { name: /registros de matr/i })).toBeVisible();
});

test('Importar planilha aparece ao lado do Exportar em /registros', async ({ page }) => {
  await page.goto('/registros');

  const importar = page.getByRole('region', { name: /importar registros/i });
  await expect(importar.getByRole('link', { name: /importar planilha/i })).toHaveAttribute(
    'href',
    '/importacao/planilha',
  );
  await expect(importar.getByRole('link', { name: /importar por foto/i })).toHaveAttribute(
    'href',
    '/importacao/imagem',
  );

  // O Exportar segue presente, no mesmo topo.
  await expect(page.getByRole('region', { name: /exportar registros/i })).toBeVisible();
});

test('o link Importar planilha leva ao fluxo de upload (.xlsx ou .csv)', async ({ page }) => {
  await page.goto('/registros');

  await page.getByRole('link', { name: /importar planilha/i }).click();

  await expect(page).toHaveURL(/\/importacao\/planilha$/);
  await expect(page.getByRole('heading', { name: /importar registros por planilha/i })).toBeVisible();
  // o seletor aceita .xlsx e .csv
  const input = page.getByLabel(/planilha de registros/i);
  await expect(input).toHaveAttribute('accept', /\.csv/);
});
