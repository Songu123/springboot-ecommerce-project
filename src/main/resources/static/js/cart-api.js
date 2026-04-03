// 🛒 Cart API Examples - JavaScript/Frontend

// ========================================
// 1. LOGIN & GET TOKEN
// ========================================

async function login(email, password) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  const data = await response.json();
  if (data.accessToken) {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('user', JSON.stringify(data.user));
    console.log('✅ Login successful!', data.user);
    return data;
  }
  console.error('❌ Login failed:', data.message);
  return null;
}

// ========================================
// 2. GET CART COUNT
// ========================================

async function getCartCount() {
  const token = localStorage.getItem('accessToken');

  const response = await fetch('/api/cart/count', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  const data = await response.json();
  if (data.success) {
    console.log(`✅ Cart count: ${data.count}`);
    return data.count;
  }
  console.error('❌ Failed to get cart count:', data.message);
  return 0;
}

// ========================================
// 3. GET MY CART
// ========================================

async function getMyCart() {
  const token = localStorage.getItem('accessToken');

  const response = await fetch('/api/cart/my-cart', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  const data = await response.json();
  if (data.success) {
    console.log('✅ Cart loaded:', data.cart);
    return data.cart;
  }
  console.error('❌ Failed to load cart:', data.message);
  return null;
}

// ========================================
// 4. ADD ITEM TO CART
// ========================================

async function addToCart(productId, quantity) {
  const token = localStorage.getItem('accessToken');

  const response = await fetch('/api/cart/add', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ productId, quantity })
  });

  const data = await response.json();
  if (data.success) {
    console.log('✅ Item added to cart:', data.cartItem);
    // Update cart count display
    updateCartCountDisplay();
    return data.cartItem;
  }
  console.error('❌ Failed to add item:', data.message);
  return null;
}

// ========================================
// 5. UPDATE CART ITEM
// ========================================

async function updateCartItem(productId, quantity) {
  const token = localStorage.getItem('accessToken');

  const response = await fetch('/api/cart/update', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ productId, quantity })
  });

  const data = await response.json();
  if (data.success) {
    console.log('✅ Item updated:', data.cartItem);
    return data.cartItem;
  }
  console.error('❌ Failed to update item:', data.message);
  return null;
}

// ========================================
// 6. REMOVE ITEM FROM CART
// ========================================

async function removeFromCart(productId) {
  const token = localStorage.getItem('accessToken');

  const response = await fetch(`/api/cart/remove/${productId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  const data = await response.json();
  if (data.success) {
    console.log('✅ Item removed from cart');
    updateCartCountDisplay();
    return true;
  }
  console.error('❌ Failed to remove item:', data.message);
  return false;
}

// ========================================
// 7. CLEAR ENTIRE CART
// ========================================

async function clearCart() {
  const token = localStorage.getItem('accessToken');

  const response = await fetch('/api/cart/clear', {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  const data = await response.json();
  if (data.success) {
    console.log('✅ Cart cleared');
    updateCartCountDisplay();
    return true;
  }
  console.error('❌ Failed to clear cart:', data.message);
  return false;
}

// ========================================
// 8. UPDATE CART DISPLAY
// ========================================

async function updateCartCountDisplay() {
  const count = await getCartCount();
  const cartBadge = document.getElementById('cartCount');
  if (cartBadge) {
    cartBadge.textContent = count;
  }
}

// ========================================
// 9. INITIALIZE ON PAGE LOAD
// ========================================

document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('accessToken');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  if (token && user.id) {
    console.log('✅ User logged in:', user.username);
    updateCartCountDisplay();
  } else {
    console.log('❌ User not logged in');
  }
});

// ========================================
// 10. EXAMPLE: ADD TO CART BUTTON
// ========================================

document.addEventListener('DOMContentLoaded', () => {
  const addToCartButtons = document.querySelectorAll('.add-to-cart-btn');

  addToCartButtons.forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.preventDefault();

      const productId = btn.dataset.productId;
      const quantity = parseInt(btn.dataset.quantity || 1);

      const token = localStorage.getItem('accessToken');
      if (!token) {
        alert('Please login first!');
        window.location.href = '/login';
        return;
      }

      const result = await addToCart(parseInt(productId), quantity);
      if (result) {
        alert('✅ Item added to cart!');
      } else {
        alert('❌ Failed to add item to cart');
      }
    });
  });
});

// ========================================
// 11. EXAMPLE: CART PAGE DISPLAY
// ========================================

async function displayCart() {
  const cart = await getMyCart();
  if (!cart) {
    document.getElementById('cartContent').innerHTML =
      '<p>❌ Failed to load cart</p>';
    return;
  }

  if (!cart.items || cart.items.length === 0) {
    document.getElementById('cartContent').innerHTML =
      '<p>Your cart is empty</p>';
    return;
  }

  let html = '<table class="cart-table"><tbody>';

  cart.items.forEach(item => {
    html += `
      <tr data-product-id="${item.productId}">
        <td>${item.productId}</td>
        <td>
          <input type="number" class="quantity-input" value="${item.quantity}" min="1">
        </td>
        <td>$${(item.price * item.quantity).toFixed(2)}</td>
        <td>
          <button class="remove-btn">Remove</button>
        </td>
      </tr>
    `;
  });

  html += '</tbody></table>';
  html += `
    <div class="cart-summary">
      <p>Total: $${cart.totalPrice.toFixed(2)}</p>
      <button class="checkout-btn">Checkout</button>
      <button class="clear-cart-btn">Clear Cart</button>
    </div>
  `;

  document.getElementById('cartContent').innerHTML = html;

  // Add event listeners
  document.querySelectorAll('.quantity-input').forEach(input => {
    input.addEventListener('change', async (e) => {
      const row = e.target.closest('tr');
      const productId = row.dataset.productId;
      const quantity = parseInt(e.target.value);

      if (quantity <= 0) {
        await removeFromCart(productId);
      } else {
        await updateCartItem(productId, quantity);
      }

      displayCart(); // Refresh display
    });
  });

  document.querySelectorAll('.remove-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      const productId = e.target.closest('tr').dataset.productId;
      await removeFromCart(productId);
      displayCart(); // Refresh display
    });
  });

  document.querySelector('.clear-cart-btn')?.addEventListener('click', async () => {
    if (confirm('Are you sure?')) {
      await clearCart();
      displayCart(); // Refresh display
    }
  });
}

// ========================================
// 12. LOGOUT FUNCTION
// ========================================

function logout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  window.location.href = '/login';
}

