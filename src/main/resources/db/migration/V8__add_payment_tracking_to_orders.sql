-- V8: track the outcome of the (mock) payment step on each order.
--
-- Until now an order only recorded the chosen `payment_method`; there was no way
-- to tell whether an online payment had actually succeeded, nor to reference it.
-- The mock gateway writes its result here so the storefront can show an honest
-- payment status instead of implying every order was paid.

ALTER TABLE public.orders
    ADD COLUMN payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN payment_reference VARCHAR(100),
    ADD COLUMN paid_at TIMESTAMP(6);

-- Existing rows can only have been cash-on-delivery style, so the default is correct.
-- Drop it afterwards so the application must set the value explicitly.
ALTER TABLE public.orders
    ALTER COLUMN payment_status DROP DEFAULT;

ALTER TABLE public.orders
    ADD CONSTRAINT chk_order_payment_status
        CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'));

-- A paid order must carry the gateway reference and the moment it was paid.
ALTER TABLE public.orders
    ADD CONSTRAINT chk_order_payment_consistency
        CHECK (
            payment_status <> 'PAID'
            OR (payment_reference IS NOT NULL AND paid_at IS NOT NULL)
        );

CREATE INDEX IF NOT EXISTS idx_orders_payment_status
    ON public.orders (payment_status);
