import { expect, test } from '@playwright/test';

test('a página de login carrega com o formulário acessível', async ({ page }) => {
  await page.goto('/login');

  await expect(page.getByRole('heading', { name: /entrar no buscarm/i })).toBeVisible();
  await expect(page.getByLabel('Usuário')).toBeVisible();
  await expect(page.getByRole('button', { name: /entrar/i })).toBeVisible();
});

test('rota protegida sem sessão redireciona para o login', async ({ page }) => {
  await page.goto('/registros');
  await expect(page).toHaveURL(/\/login$/);
});
