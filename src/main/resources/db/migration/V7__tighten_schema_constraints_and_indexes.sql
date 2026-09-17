-- V7: corrective migration.
--
-- Migrations that have already been applied are immutable, so the three schema defects
-- introduced by V1/V3 are corrected here, in a new versioned migration, instead of by
-- rewriting history:
--
--   1. `users.email` carried TWO identical unique constraints (`idx_user_email` and
--      `users_email_key`). Every insert/update maintained the same index twice.
--   2. `accessories.stock` / `accessories.price` were only validated in the service
--      layer, so the database happily accepted negative stock or a zero/negative price.
--   3. PostgreSQL does not create indexes for foreign-key columns automatically, which
--      makes parent deletes and joins scan the child tables. Only FK columns that are
--      not already the leading column of a PK/unique index are indexed below.

-- 1. Keep the conventionally named constraint, drop the duplicate.
ALTER TABLE public.users
    DROP CONSTRAINT IF EXISTS idx_user_email;

-- 2. Enforce the stock/price invariants at the database boundary.
ALTER TABLE public.accessories
    ADD CONSTRAINT chk_accessory_stock_non_negative CHECK (stock >= 0);

ALTER TABLE public.accessories
    ADD CONSTRAINT chk_accessory_price_positive CHECK (price > 0);

-- 3. Index foreign keys that have no covering index.
CREATE INDEX IF NOT EXISTS idx_accessories_category_id
    ON public.accessories (category_id);

CREATE INDEX IF NOT EXISTS idx_cart_items_accessory_id
    ON public.cart_items (accessory_id);

CREATE INDEX IF NOT EXISTS idx_order_items_accessory_id
    ON public.order_items (accessory_id);

CREATE INDEX IF NOT EXISTS idx_orders_user_id
    ON public.orders (user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id
    ON public.refresh_tokens (user_id);

CREATE INDEX IF NOT EXISTS idx_reviews_accessory_id
    ON public.reviews (accessory_id);

CREATE INDEX IF NOT EXISTS idx_users_roles_mapping_user_id
    ON public.users_roles_mapping (user_id);

CREATE INDEX IF NOT EXISTS idx_wishlist_items_accessory_id
    ON public.wishlist_items (accessory_id);
