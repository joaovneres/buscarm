import { expect, test } from '@playwright/test';

test('a página de busca carrega com o formulário acessível', async ({ page }) => {
  await page.goto('/registros');

  await expect(page.getByRole('heading', { name: /buscar registro de matrícula/i })).toBeVisible();
  await expect(page.getByRole('textbox')).toBeVisible();
  await expect(page.getByRole('button', { name: /buscar/i })).toBeVisible();
});
