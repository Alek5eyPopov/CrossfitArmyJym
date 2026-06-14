# Подтверждение email в Supabase

Supabase использует публичную страницу GitHub Pages:

```text
https://alek5eypopov.github.io/CrossfitArmyJym/email-confirmed/
```

После подтверждения пользователь видит фирменную веб-страницу. Кнопка на ней открывает
Android-приложение через `crossfitarmyjym://email-confirmed`. Если приложение не
установлено или письмо открыто на компьютере, пользователь остаётся на информативной странице.

## 1. Разрешить redirect URL

1. Открыть Supabase Dashboard.
2. Перейти в `Authentication` → `URL Configuration`.
3. Добавить в `Redirect URLs`:

   ```text
   https://alek5eypopov.github.io/CrossfitArmyJym/email-confirmed/
   ```

4. Сохранить изменения.

Регистрация передаёт этот адрес параметром `redirect_to`. Если адрес не добавлен в
список разрешённых, Supabase использует `Site URL`, и фирменная страница после письма
не откроется.

## 2. Включить GitHub Pages

1. Открыть репозиторий GitHub → `Settings` → `Pages`.
2. В разделе `Build and deployment` выбрать `Deploy from a branch`.
3. Выбрать ветку `main` и папку `/docs`.
4. Нажать `Save`.
5. Дождаться публикации адреса:

   ```text
   https://alek5eypopov.github.io/CrossfitArmyJym/email-confirmed/
   ```

Исходник страницы находится в `docs/email-confirmed/index.html`.

## 3. Установить шаблон письма

1. Открыть `Authentication` → `Email Templates`.
2. Выбрать шаблон `Confirm signup`.
3. В поле темы указать:

   ```text
   Подтвердите регистрацию в CrossFit ARMY
   ```

4. Скопировать содержимое файла
   `database/email_templates/confirm_signup.html` в поле шаблона.
5. Сохранить шаблон.

Шаблон должен использовать `{{ .ConfirmationURL }}`. Эта ссылка сначала подтверждает
пользователя в Supabase, а затем перенаправляет его на публичную страницу результата.

## 4. Проверка

1. Установить свежую сборку приложения на смартфон или эмулятор.
2. Зарегистрировать новый email.
3. Открыть полученное письмо на устройстве с установленным приложением.
4. Нажать `ПОДТВЕРДИТЬ EMAIL`.
5. Убедиться, что в браузере открылась фирменная страница `ПОЧТА ПОДТВЕРЖДЕНА`.
6. На Android-устройстве нажать `ОТКРЫТЬ ПРИЛОЖЕНИЕ`.
7. Убедиться, что открылась `EmailConfirmedActivity`, затем перейти ко входу.

Для повторного теста нужен новый адрес либо удаление тестового пользователя в
`Authentication` → `Users`.
