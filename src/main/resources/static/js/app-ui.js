/**
 * LifestyleShop - Premium App UI Script
 * AJAX Cart + Toast + Flash Timer
 */

/* ===== 1. INIT ===== */
document.addEventListener('DOMContentLoaded', () => {
    initCartBadge();
    initAjaxCartButtons();
    initFlashTimer();
    initQtyControls();
});


/* ===== 3. CART BADGE ===== */
function initCartBadge() {
    updateCartBadge();
}

function updateCartBadge() {
    fetch('/api/cart/count')
        .then(r => r.ok ? r.json() : Promise.reject())
        .then(data => {
            document.querySelectorAll('.cart-count').forEach(el => {
                const count = data.count || 0;
                el.textContent = count;
                el.style.display = count === 0 ? 'none' : 'flex';
            });
        })
        .catch(() => {
            document.querySelectorAll('.cart-count').forEach(el => el.style.display = 'none');
        });
}

function bumpCartBadge() {
    document.querySelectorAll('.nav-cart-badge').forEach(el => {
        el.classList.add('bump');
        setTimeout(() => el.classList.remove('bump'), 350);
    });
}

/* ===== 4. AJAX ADD TO CART ===== */
function initAjaxCartButtons() {
    document.querySelectorAll('.btn-add-cart-ajax, .btn-add-cart').forEach(btn => {
        btn.addEventListener('click', handleAddToCart);
    });
}

function handleAddToCart(e) {
    e.preventDefault();
    const btn = this || e.currentTarget;
    const productId = btn.dataset.productId;
    const qtyInput = document.getElementById('qty-main');
    const quantity = qtyInput ? parseInt(qtyInput.value) || 1 : 1;

    if (!productId) return;

    // Loading state
    btn.disabled = true;
    const originalHtml = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang thêm...';

    fetch('/api/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ productId: parseInt(productId), quantity })
    })
    .then(r => {
        if (r.ok) return r.json();
        return r.json().then(d => Promise.reject(d.message || 'Lỗi'));
    })
    .then(() => {
        showToast('Thành công', 'Sản phẩm đã được thêm vào giỏ hàng!', 'success');
        updateCartBadge();
        bumpCartBadge();
    })
    .catch(msg => {
        if (msg && msg.toString().includes('authenticated')) {
            showToast('Yêu cầu đăng nhập', 'Vui lòng đăng nhập để thêm vào giỏ hàng.', 'info');
            setTimeout(() => window.location.href = '/login', 1500);
        } else {
            showToast('Lỗi', msg || 'Không thể thêm vào giỏ hàng.', 'error');
        }
    })
    .finally(() => {
        btn.disabled = false;
        btn.innerHTML = originalHtml;
    });
}

/* ===== 5. AJAX CART PAGE ACTIONS ===== */
function ajaxUpdateQty(productId, quantity) {
    return fetch('/api/cart/update', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ productId, quantity })
    }).then(r => r.json());
}

function ajaxRemoveItem(productId) {
    return fetch(`/api/cart/remove/${productId}`, { 
        method: 'DELETE',
        credentials: 'same-origin'
    }).then(r => r.json());
}

/* Bind cart page buttons if present */
document.addEventListener('DOMContentLoaded', () => {
    // Qty +/- buttons on cart page
    document.querySelectorAll('.cart-qty-inc').forEach(btn => {
        btn.addEventListener('click', function () {
            const pid = this.dataset.productId;
            const input = document.getElementById('qty-' + pid);
            if (!input) return;
            const newVal = parseInt(input.value) + 1;
            input.value = newVal;
            throttledUpdate(pid, newVal, this);
        });
    });

    document.querySelectorAll('.cart-qty-dec').forEach(btn => {
        btn.addEventListener('click', function () {
            const pid = this.dataset.productId;
            const input = document.getElementById('qty-' + pid);
            if (!input) return;
            const newVal = Math.max(1, parseInt(input.value) - 1);
            input.value = newVal;
            throttledUpdate(pid, newVal, this);
        });
    });

    document.querySelectorAll('.cart-qty-input').forEach(input => {
        input.addEventListener('change', function () {
            const pid = this.dataset.productId;
            const val = Math.max(1, parseInt(this.value) || 1);
            this.value = val;
            throttledUpdate(pid, val, this);
        });
    });

    document.querySelectorAll('.cart-remove-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const pid = this.dataset.productId;
            const row = document.getElementById('cart-row-' + pid);
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            ajaxRemoveItem(pid).then(data => {
                if (data.success) {
                    row && row.remove();
                    updateCartBadge();
                    refreshCartTotals();
                    showToast('Đã xóa', 'Sản phẩm đã bị xóa khỏi giỏ hàng.', 'info');
                    checkEmptyCart();
                } else {
                    showToast('Lỗi', data.message || 'Không xóa được.', 'error');
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fas fa-trash"></i>';
                }
            }).catch(() => { btn.disabled = false; btn.innerHTML = '<i class="fas fa-trash"></i>'; });
        });
    });
});

let updateTimers = {};
function throttledUpdate(pid, qty, el) {
    clearTimeout(updateTimers[pid]);
    updateTimers[pid] = setTimeout(() => {
        ajaxUpdateQty(parseInt(pid), qty).then(data => {
            if (data.success) {
                refreshCartTotals();
                updateCartBadge();
                // Update row subtotal
                const subEl = document.getElementById('subtotal-' + pid);
                if (subEl && data.cartItem) {
                    const sub = data.cartItem.price * qty;
                    subEl.textContent = formatVND(sub);
                }
            } else {
                showToast('Lỗi', data.message || 'Cập nhật thất bại.', 'error');
            }
        }).catch(() => showToast('Lỗi', 'Mất kết nối.', 'error'));
    }, 600);
}

function refreshCartTotals() {
    const rows = document.querySelectorAll('.cart-item-row[data-price]');
    let subtotal = 0;
    rows.forEach(row => {
        const price = parseFloat(row.dataset.price) || 0;
        const pid = row.dataset.productId;
        const input = document.getElementById('qty-' + pid);
        const qty = input ? parseInt(input.value) || 1 : 1;
        subtotal += price * qty;
    });
    const shipping = subtotal >= 500000 ? 0 : 30000;
    const total = subtotal + shipping;

    safeSet('cart-subtotal', formatVND(subtotal));
    safeSet('cart-shipping', shipping === 0 ? 'MIỄN PHÍ' : formatVND(shipping));
    safeSet('cart-total', formatVND(total));
}

function checkEmptyCart() {
    const rows = document.querySelectorAll('.cart-item-row');
    const emptyState = document.getElementById('cart-empty');
    const cartContent = document.getElementById('cart-content');
    if (rows.length === 0) {
        emptyState && (emptyState.style.display = 'block');
        cartContent && (cartContent.style.display = 'none');
    }
}

/* ===== 6. TOAST ===== */
function showToast(title, message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    const toast = document.createElement('div');
    toast.className = `toast-ls ${type}`;
    toast.innerHTML = `
        <div class="toast-icon-wrap">${icons[type] || 'ℹ️'}</div>
        <div class="toast-content">
            <strong>${title}</strong>
            <span>${message}</span>
        </div>
    `;
    container.appendChild(toast);
    requestAnimationFrame(() => requestAnimationFrame(() => toast.classList.add('show')));
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

/* ===== 7. FLASH SALE TIMER ===== */
function initFlashTimer() {
    const el = document.getElementById('flashTimer');
    if (!el) return;
    let totalSec = 2 * 3600 + 45 * 60 + 30;
    const tick = () => {
        if (totalSec <= 0) return;
        totalSec--;
        const h = String(Math.floor(totalSec / 3600)).padStart(2, '0');
        const m = String(Math.floor((totalSec % 3600) / 60)).padStart(2, '0');
        const s = String(totalSec % 60).padStart(2, '0');
        el.textContent = `${h}:${m}:${s}`;
    };
    setInterval(tick, 1000);
}

/* ===== 8. QTY CONTROLS (product detail) ===== */
function initQtyControls() {
    const input = document.getElementById('qty-main');
    if (!input) return;
    document.getElementById('qty-inc')?.addEventListener('click', () => {
        input.value = Math.min(99, parseInt(input.value) + 1);
    });
    document.getElementById('qty-dec')?.addEventListener('click', () => {
        input.value = Math.max(1, parseInt(input.value) - 1);
    });
}

/* ===== 9. UTILS ===== */
function formatVND(n) {
    return new Intl.NumberFormat('vi-VN').format(Math.round(n)) + ' VNĐ';
}

function safeSet(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

/* Password toggle */
function togglePwd(inputId, btnId) {
    const input = document.getElementById(inputId);
    const icon = document.querySelector(`#${btnId} i`);
    if (!input) return;
    input.type = input.type === 'password' ? 'text' : 'password';
    if (icon) icon.className = input.type === 'password' ? 'fas fa-eye' : 'fas fa-eye-slash';
}

/* Password strength (register page) */
function checkPasswordStrength(inputId) {
    const val = document.getElementById(inputId)?.value || '';
    let score = 0;
    if (val.length >= 8) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    for (let i = 1; i <= 4; i++) {
        const bar = document.getElementById('sbar' + i);
        if (bar) {
            bar.className = 'strength-bar' + (i <= score ? ` active-${score}` : '');
        }
    }
    const txt = document.getElementById('strength-text');
    if (txt) {
        const labels = ['', 'Yếu', 'Trung bình', 'Khá', 'Mạnh'];
        txt.textContent = labels[score] || '';
        txt.style.color = ['', 'var(--clr-danger)','var(--clr-warning)','var(--clr-primary-light)','var(--clr-success)'][score];
    }
}

/* Password strength (change-password page - separate IDs) */
function checkCpPasswordStrength() {
    const val = document.getElementById('newPassword')?.value || '';
    let score = 0;
    if (val.length >= 6) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    for (let i = 1; i <= 4; i++) {
        const bar = document.getElementById('cpbar' + i);
        if (bar) {
            bar.className = 'strength-bar' + (i <= score ? ` active-${score}` : '');
        }
    }
    const txt = document.getElementById('cp-strength-text');
    if (txt) {
        const labels = ['', 'Yếu', 'Trung bình', 'Khá', 'Mạnh'];
        txt.textContent = labels[score] || '';
        txt.style.color = ['', 'var(--clr-danger)','var(--clr-warning)','var(--clr-primary-light)','var(--clr-success)'][score];
    }
}
