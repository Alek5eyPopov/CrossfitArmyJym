-- ============================================================
-- CrossFit ARMY demo news
-- ============================================================
-- Run after database/migrations/20260709_stage10_news.sql.
-- The script is idempotent: repeated runs update the same demo posts.
-- It uses public HTTPS image URLs so the news feed can be tested without
-- manually uploading files to Supabase Storage.
-- ============================================================

BEGIN;

DO $$
DECLARE
    v_admin_id UUID;
BEGIN
    IF to_regclass('public.news_posts') IS NULL THEN
        RAISE EXCEPTION
            'Table public.news_posts does not exist. Apply 20260709_stage10_news.sql first.';
    END IF;

    SELECT p.id
    INTO v_admin_id
    FROM public.profiles AS p
    WHERE p.role = 'admin'
      AND p.is_active
    ORDER BY p.created_at
    LIMIT 1;

    INSERT INTO public.news_posts (
        id,
        title,
        summary,
        body,
        image_url,
        status,
        published_at,
        created_by
    )
    VALUES
        (
            '90000000-0000-0000-0000-000000000001',
            'Запущено мобильное приложение ARMY',
            'Теперь расписание, записи на тренировки и новости клуба доступны прямо в телефоне.',
            'Мы запустили первую тестовую версию мобильного приложения ARMY. В нем уже можно смотреть актуальное расписание занятий, отслеживать свои записи и читать новости клуба. Это только начало: дальше будем добавлять личный прогресс, результаты WOD, уведомления и удобные инструменты для тренеров.',
            'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80',
            'published',
            now() - interval '5 days',
            v_admin_id
        ),
        (
            '90000000-0000-0000-0000-000000000002',
            'Новая неделя тренировок',
            'В расписании появились силовые блоки, техника гимнастики и несколько интенсивных WOD.',
            'На этой неделе делаем акцент на базовую силу, качественную разминку и стабильную технику. В расписании появятся тренировки с тягой, жимовыми движениями, подтягиваниями и интервальной работой. Если сомневаетесь в нагрузке, выбирайте optional-вариант и обязательно сообщайте тренеру о самочувствии.',
            'https://images.unsplash.com/photo-1534258936925-c58bed479fcb?auto=format&fit=crop&w=1200&q=80',
            'published',
            now() - interval '4 days',
            v_admin_id
        ),
        (
            '90000000-0000-0000-0000-000000000003',
            'День техники: становая тяга',
            'Разбираем стартовую позицию, дыхание, фиксацию корпуса и безопасный прогресс веса.',
            'В субботу проведем отдельный технический блок по становой тяге. Тренеры помогут настроить стартовую позицию, подобрать рабочий вес и разобрать типичные ошибки. Формат подойдет и новичкам, и опытным атлетам, которые хотят сделать движение чище и увереннее.',
            'https://images.unsplash.com/photo-1605296867304-46d5465a13f1?auto=format&fit=crop&w=1200&q=80',
            'published',
            now() - interval '3 days',
            v_admin_id
        ),
        (
            '90000000-0000-0000-0000-000000000004',
            'Обновление зоны восстановления',
            'Добавили больше места для растяжки, роллов и спокойной заминки после тренировки.',
            'После интенсивного WOD восстановление так же важно, как сама работа. Мы обновили зону заминки: добавили коврики, роллы и свободное пространство для растяжки. Пожалуйста, оставляйте инвентарь на своих местах после использования, чтобы зона оставалась удобной для всех.',
            'https://images.unsplash.com/photo-1599058917212-d750089bc07e?auto=format&fit=crop&w=1200&q=80',
            'published',
            now() - interval '2 days',
            v_admin_id
        ),
        (
            '90000000-0000-0000-0000-000000000005',
            'ARMY Challenge в конце месяца',
            'Готовим клубный челлендж с командным WOD, таблицей результатов и небольшими призами.',
            'В конце месяца проведем ARMY Challenge. Формат будет командным: несколько станций, понятная шкала нагрузки и отдельный зачет для новичков. Подробности появятся ближе к дате старта, а пока тренируйтесь стабильно и не пропускайте восстановление.',
            'https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=1200&q=80',
            'published',
            now() - interval '1 day',
            v_admin_id
        ),
        (
            '90000000-0000-0000-0000-000000000006',
            'Черновик: летняя акция',
            'Эта новость должна быть видна только администратору до публикации.',
            'Черновик новости для проверки административного интерфейса. Клиент и тренер не должны видеть этот пост в общей ленте, пока статус не изменен на published.',
            'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=1200&q=80',
            'draft',
            NULL,
            v_admin_id
        )
    ON CONFLICT (id)
    DO UPDATE SET
        title = EXCLUDED.title,
        summary = EXCLUDED.summary,
        body = EXCLUDED.body,
        image_url = EXCLUDED.image_url,
        status = EXCLUDED.status,
        published_at = EXCLUDED.published_at,
        created_by = COALESCE(news_posts.created_by, EXCLUDED.created_by),
        updated_at = now();

    RAISE NOTICE 'Demo news seed complete. Admin author: %', COALESCE(v_admin_id::TEXT, 'none');
END;
$$;

COMMIT;
