-- ============================================================
-- Stage 10: News posts with images
-- ============================================================

CREATE TABLE IF NOT EXISTS public.news_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL CHECK (length(btrim(title)) > 0),
    summary TEXT,
    body TEXT NOT NULL CHECK (length(btrim(body)) > 0),
    image_url TEXT,
    status TEXT NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft', 'published', 'archived')),
    published_at TIMESTAMPTZ,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_news_posts_status_published
    ON public.news_posts(status, published_at DESC);

CREATE OR REPLACE FUNCTION public.set_news_post_defaults()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    NEW.updated_at := now();

    IF TG_OP = 'INSERT' THEN
        NEW.created_by := COALESCE(NEW.created_by, (SELECT auth.uid()));
    END IF;

    IF NEW.status = 'published' AND NEW.published_at IS NULL THEN
        NEW.published_at := now();
    END IF;

    IF NEW.status <> 'published' THEN
        NEW.published_at := NULL;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS set_news_post_defaults ON public.news_posts;
CREATE TRIGGER set_news_post_defaults
BEFORE INSERT OR UPDATE ON public.news_posts
FOR EACH ROW EXECUTE FUNCTION public.set_news_post_defaults();

ALTER TABLE public.news_posts ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Admins can manage news posts" ON public.news_posts;
DROP POLICY IF EXISTS "Authenticated users can view published news posts" ON public.news_posts;

CREATE POLICY "Authenticated users can view published news posts"
ON public.news_posts
FOR SELECT TO authenticated
USING (
    status = 'published'
    OR public.is_admin()
);

CREATE POLICY "Admins can manage news posts"
ON public.news_posts
FOR ALL TO authenticated
USING (public.is_admin())
WITH CHECK (public.is_admin());

GRANT SELECT, INSERT, UPDATE, DELETE ON public.news_posts TO authenticated;

INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'news-images',
    'news-images',
    true,
    5242880,
    ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id)
DO UPDATE SET
    public = EXCLUDED.public,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

DROP POLICY IF EXISTS "Admins can upload news images" ON storage.objects;
DROP POLICY IF EXISTS "Admins can update news images" ON storage.objects;
DROP POLICY IF EXISTS "Admins can delete news images" ON storage.objects;
DROP POLICY IF EXISTS "Anyone can read news images" ON storage.objects;

CREATE POLICY "Anyone can read news images"
ON storage.objects
FOR SELECT
USING (bucket_id = 'news-images');

CREATE POLICY "Admins can upload news images"
ON storage.objects
FOR INSERT TO authenticated
WITH CHECK (
    bucket_id = 'news-images'
    AND public.is_admin()
);

CREATE POLICY "Admins can update news images"
ON storage.objects
FOR UPDATE TO authenticated
USING (
    bucket_id = 'news-images'
    AND public.is_admin()
)
WITH CHECK (
    bucket_id = 'news-images'
    AND public.is_admin()
);

CREATE POLICY "Admins can delete news images"
ON storage.objects
FOR DELETE TO authenticated
USING (
    bucket_id = 'news-images'
    AND public.is_admin()
);
