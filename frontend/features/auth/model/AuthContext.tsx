'use client';

import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';

import { login as apiLogin } from '@/entities/usuario/api/login';
import {
  getToken,
  getUsuario,
  limparSessao,
  salvarSessao,
  type UsuarioSessao,
} from '@/shared/auth/session';

interface AuthContexto {
  usuario: UsuarioSessao | null;
  autenticado: boolean;
  carregando: boolean;
  entrar: (email: string, senha: string) => Promise<void>;
  sair: () => void;
}

const Contexto = createContext<AuthContexto | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioSessao | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    if (getToken()) {
      setUsuario(getUsuario());
    }
    setCarregando(false);
  }, []);

  async function entrar(email: string, senha: string) {
    const r = await apiLogin(email, senha);
    const u: UsuarioSessao = { nome: r.nome, email: r.email, papel: r.papel };
    salvarSessao(r.token, u);
    setUsuario(u);
  }

  function sair() {
    limparSessao();
    setUsuario(null);
  }

  return (
    <Contexto.Provider
      value={{ usuario, autenticado: usuario !== null, carregando, entrar, sair }}
    >
      {children}
    </Contexto.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(Contexto);
  if (!ctx) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return ctx;
}
