/**
 * StockTradingPlatform - Frontend Application Logic (Modern Fintech Theme)
 * Role separation for Admin (Products & Users only, no wishlist/basket/balance)
 * and User (Marketplace, Wishlist, Basket, Payment, Balance top-up)
 */

const CATEGORY_NAMES = {
    'DAIRY_PRODUCTS': 'Dairy Products',
    'MEAT': 'Meat & Poultry',
    'VEGETABLES': 'Fresh Vegetables',
    'FRUITS': 'Organic Fruits',
    'BAKERY': 'Bakery & Bread',
    'BEVERAGES': 'Beverages & Drinks',
    'SNACKS': 'Snacks & Bites',
    'FROZEN_FOOD': 'Frozen Foods',
    'CANNED_FOOD': 'Canned Goods',
    'CLEANING_PRODUCTS': 'Cleaning Supplies',
    'PERSONAL_CARE': 'Personal Care'
};

const app = {
    user: null,
    products: [],
    usersList: [],
    favorites: [],
    basketItems: [],
    currentCategory: 'ALL',
    searchQuery: '',
    currentSort: 'default',
    productQtyMap: {},

    // Admin state
    adminCurrentSubtab: 'products',
    adminProdSearch: '',
    adminProdCategory: 'ALL',
    adminProdSort: 'default',
    adminUserSearch: '',
    adminUserSort: 'default',

    // ==================== INITIALIZATION ====================
    init: async function() {
        this.loadStoredUser();
        await this.loadProducts();

        if (this.user && this.user.token) {
            await this.refreshUserData();
            if (this.user.role === 'ROLE_ADMIN') {
                this.showTab('admin');
                await this.loadAdminUsers();
            } else {
                await this.loadFavorites();
                await this.loadBasket();
                this.showTab('market');
            }
        } else {
            this.showTab('market');
        }

        this.renderNavAuth();
    },

    handleBrandClick: function() {
        if (this.user && this.user.role === 'ROLE_ADMIN') {
            this.showTab('admin');
        } else {
            this.showTab('market');
        }
    },

    loadStoredUser: function() {
        const stored = localStorage.getItem('stock_user');
        if (stored) {
            try {
                this.user = JSON.parse(stored);
                if (!this.user || !this.user.token) {
                    this.user = null;
                    localStorage.removeItem('stock_user');
                }
            } catch (e) {
                this.user = null;
            }
        }
    },

    setSessionUser: function(userData) {
        this.user = userData;
        localStorage.setItem('stock_user', JSON.stringify(userData));
        this.renderNavAuth();

        if (this.user.role === 'ROLE_ADMIN') {
            this.showTab('admin');
            this.loadAdminData();
        } else {
            this.showTab('market');
            this.loadFavorites();
            this.loadBasket();
            this.renderProducts();
        }
    },

    clearSessionUser: function() {
        this.user = null;
        localStorage.removeItem('stock_user');
        this.favorites = [];
        this.basketItems = [];
        this.renderNavAuth();
        this.updateBadges();
        this.renderProducts();
        this.showTab('market');
    },

    // ==================== API CALLER ====================
    api: async function(url, options = {}) {
        const headers = options.headers || {};
        if (this.user && this.user.token) {
            headers['Authorization'] = `Bearer ${this.user.token}`;
        }
        if (!(options.body instanceof FormData) && !headers['Content-Type'] && options.body) {
            headers['Content-Type'] = 'application/json';
        }

        try {
            const response = await fetch(url, { ...options, headers });

            if (response.status === 401 || response.status === 403) {
                if (this.user && response.status === 401) {
                    this.toast('Your session has expired. Please sign in again.', 'error');
                    this.clearSessionUser();
                }
                const contentType = response.headers.get("content-type");
                let errData;
                if (contentType && contentType.includes("application/json")) {
                    errData = await response.json();
                } else {
                    errData = await response.text();
                }
                const msg = (errData && errData.error) ? errData.error : (typeof errData === 'string' && errData ? errData : (response.status === 403 ? 'Access denied (403 Forbidden)' : 'Authentication required'));
                throw new Error(msg);
            }

            const contentType = response.headers.get("content-type");
            let data;
            if (contentType && contentType.includes("application/json")) {
                data = await response.json();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                const errorMsg = (data && data.error) ? data.error : (typeof data === 'string' && data ? data : 'An unexpected error occurred');
                throw new Error(errorMsg);
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            this.toast(error.message, 'error');
            throw error;
        }
    },

    // ==================== AUTHENTICATION ====================
    handleLogin: async function(e) {
        e.preventDefault();
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        try {
            const res = await this.api('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password })
            });

            if (res && res.token) {
                this.setSessionUser(res);
                this.closeAuthModal();
                const roleLabel = res.role === 'ROLE_ADMIN' ? 'Administrator' : 'Customer';
                this.toast(`Welcome back, ${res.name}! (${roleLabel}) 🚀`, 'success');
            }
        } catch (err) {}
    },

    handleRegister: async function(e) {
        e.preventDefault();
        const data = {
            name: document.getElementById('reg-name').value.trim(),
            surname: document.getElementById('reg-surname').value.trim(),
            age: document.getElementById('reg-age').value.trim(),
            phoneNumber: document.getElementById('reg-phone').value.trim(),
            email: document.getElementById('reg-email').value.trim(),
            password: document.getElementById('reg-password').value,
            role: 'ROLE_USER',
            balance: parseFloat(document.getElementById('reg-balance').value) || 0
        };

        try {
            const res = await this.api('/auth/register', {
                method: 'POST',
                body: JSON.stringify(data)
            });

            if (res && res.token) {
                this.setSessionUser(res);
                this.closeAuthModal();
                this.toast(`Account successfully created! Welcome, ${res.name}. 🎉`, 'success');
            }
        } catch (err) {}
    },

    quickAdminLogin: async function() {
        document.getElementById('login-email').value = 'admin@stock.com';
        document.getElementById('login-password').value = 'admin123';

        try {
            const res = await this.api('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email: 'admin@stock.com', password: 'admin123' })
            });
            if (res && res.token) {
                this.setSessionUser(res);
                this.toast('Signed in as System Administrator! 👑', 'success');
            }
        } catch (e) {}
    },

    refreshUserData: async function() {
        if (!this.user || !this.user.token) return;
        try {
            const res = await this.api('/auth/me');
            if (res) {
                const currentToken = res.token || this.user.token;
                this.user = { ...this.user, ...res, token: currentToken };
                localStorage.setItem('stock_user', JSON.stringify(this.user));
                this.renderNavAuth();
            }
        } catch (e) {}
    },

    logout: function() {
        this.clearSessionUser();
        this.toast('You have successfully signed out', 'info');
    },

    // ==================== WALLET / DEPOSIT (USERS ONLY) ====================
    quickDeposit: async function(amount) {
        if (!this.user) {
            this.openAuthModal('login');
            return;
        }
        try {
            const res = await this.api('/auth/deposit', {
                method: 'POST',
                body: JSON.stringify({ amount: parseFloat(amount) })
            });
            if (res) {
                const currentToken = res.token || this.user.token;
                this.user = { ...this.user, ...res, token: currentToken };
                localStorage.setItem('stock_user', JSON.stringify(this.user));
                this.renderNavAuth();
                this.closeDepositModal();
                this.toast(`Added +$${amount} to your wallet! New balance: $${Number(this.user.balance).toFixed(2)} 💳`, 'success');
            }
        } catch (e) {}
    },

    handleCustomDeposit: function(e) {
        e.preventDefault();
        const amt = document.getElementById('deposit-amount-input').value;
        if (amt && parseFloat(amt) > 0) {
            this.quickDeposit(amt);
        }
    },

    // ==================== MARKETPLACE PRODUCTS ====================
    loadProducts: async function() {
        try {
            const data = await this.api('/products');
            this.products = Array.isArray(data) ? data : [];
            this.renderProducts();
            if (this.user && this.user.role === 'ROLE_ADMIN') {
                this.renderAdminProducts();
            }
        } catch (e) {
            this.products = [];
            this.renderProducts();
        }
    },

    filterCategory: function(category) {
        this.currentCategory = category;
        document.querySelectorAll('.category-pill').forEach(pill => {
            pill.classList.remove('active');
        });
        if (window.event && window.event.currentTarget) {
            window.event.currentTarget.classList.add('active');
        }
        this.renderProducts();
    },

    handleSearch: function(query) {
        this.searchQuery = query.trim().toLowerCase();
        const clearBtn = document.getElementById('search-clear-btn');
        if (clearBtn) clearBtn.style.display = this.searchQuery ? 'block' : 'none';
        this.renderProducts();
    },

    clearSearch: function() {
        const input = document.getElementById('global-search');
        if (input) input.value = '';
        this.handleSearch('');
    },

    handleSort: function(sortType) {
        this.currentSort = sortType;
        this.renderProducts();
    },

    getFilteredProducts: function() {
        let list = [...this.products];

        if (this.currentCategory !== 'ALL') {
            list = list.filter(p => p.productCategory === this.currentCategory);
        }

        if (this.searchQuery) {
            list = list.filter(p => (p.name && p.name.toLowerCase().includes(this.searchQuery)) ||
                                     (p.productCategory && p.productCategory.toLowerCase().includes(this.searchQuery)));
        }

        if (this.currentSort === 'price-asc') {
            list.sort((a, b) => (Number(a.price) || 0) - (Number(b.price) || 0));
        } else if (this.currentSort === 'price-desc') {
            list.sort((a, b) => (Number(b.price) || 0) - (Number(a.price) || 0));
        } else if (this.currentSort === 'name-asc') {
            list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
        } else if (this.currentSort === 'name-desc') {
            list.sort((a, b) => (b.name || '').localeCompare(a.name || ''));
        } else if (this.currentSort === 'stock-desc') {
            list.sort((a, b) => (b.stock || 0) - (a.stock || 0));
        }

        return list;
    },

    renderProducts: function() {
        const grid = document.getElementById('products-grid');
        if (!grid) return;

        const filtered = this.getFilteredProducts();

        if (filtered.length === 0) {
            grid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <i class="fa-solid fa-box-open"></i>
                    <h3>No products found</h3>
                    <p class="text-muted">Try changing your search terms or selecting a different category.</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = filtered.map(p => {
            const isFav = this.favorites.some(f => f.id === p.id);
            const categoryLabel = CATEGORY_NAMES[p.productCategory] || p.productCategory;
            const inStock = p.stock > 0;
            const currentQty = this.productQtyMap[p.id] || 1;
            const fallbackImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';

            return `
                <div class="product-card">
                    <div class="product-img-wrap">
                        <img class="product-img" src="${p.image || fallbackImg}" alt="${p.name}" onerror="this.src='${fallbackImg}'">
                        <span class="product-category-tag">${categoryLabel}</span>
                        ${(!this.user || this.user.role !== 'ROLE_ADMIN') ? `
                            <button class="product-fav-btn ${isFav ? 'active' : ''}" onclick="app.toggleFavorite(${p.id})" title="${isFav ? 'Remove from Wishlist' : 'Add to Wishlist'}">
                                <i class="fa-${isFav ? 'solid' : 'regular'} fa-heart"></i>
                            </button>
                        ` : ''}
                    </div>
                    <div class="product-info">
                        <h3 class="product-title">${p.name}</h3>
                        <div class="product-stock-line">
                            <span class="stock-dot ${inStock ? 'dot-green' : 'dot-red'}"></span>
                            <span class="${inStock ? 'stock-in' : 'stock-out'}">
                                ${inStock ? `In Stock: ${p.stock} units` : 'Out of stock'}
                            </span>
                        </div>
                        <div class="product-footer">
                            <div class="product-price">$${Number(p.price).toFixed(2)}</div>
                            <div class="product-actions">
                                ${(!this.user || this.user.role !== 'ROLE_ADMIN') ? (
                                    inStock ? `
                                        <div class="qty-control">
                                            <button class="qty-btn" onclick="app.changeQty(${p.id}, -1)">-</button>
                                            <span class="qty-num" id="qty-${p.id}">${currentQty}</span>
                                            <button class="qty-btn" onclick="app.changeQty(${p.id}, 1)">+</button>
                                        </div>
                                        <button class="btn btn-accent btn-sm btn-glow" onclick="app.addToBasket(${p.id})">
                                            <i class="fa-solid fa-cart-plus"></i> Add
                                        </button>
                                    ` : `
                                        <button class="btn btn-outline btn-sm" disabled>Sold Out</button>
                                    `
                                ) : `
                                    <button class="btn btn-outline btn-sm" onclick="app.showTab('admin')">
                                        <i class="fa-solid fa-gear"></i> Manage
                                    </button>
                                `}
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    changeQty: function(productId, delta) {
        let qty = (this.productQtyMap[productId] || 1) + delta;
        if (qty < 1) qty = 1;
        this.productQtyMap[productId] = qty;
        const el = document.getElementById(`qty-${productId}`);
        if (el) el.innerText = qty;
    },

    // ==================== WISHLIST / FAVORITES (USERS ONLY) ====================
    loadFavorites: async function() {
        if (!this.user || !this.user.id || this.user.role === 'ROLE_ADMIN') {
            this.favorites = [];
            this.updateBadges();
            return;
        }
        try {
            const data = await this.api(`/favorites/all/${this.user.id}`);
            this.favorites = Array.isArray(data) ? data : [];
            this.updateBadges();
            this.renderFavorites();
        } catch (e) {
            this.favorites = [];
            this.updateBadges();
        }
    },

    toggleFavorite: async function(productId) {
        if (!this.user || !this.user.id) {
            this.toast('Please sign in to save items to your wishlist', 'info');
            this.openAuthModal('login');
            return;
        }

        try {
            await this.api(`/favorites/${this.user.id}/${productId}`, { method: 'POST' });
            await this.loadFavorites();
            this.renderProducts();
            this.toast('Wishlist updated ❤️', 'success');
        } catch (e) {}
    },

    renderFavorites: function() {
        const grid = document.getElementById('favorites-grid');
        if (!grid) return;

        if (this.favorites.length === 0) {
            grid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <i class="fa-solid fa-heart-crack"></i>
                    <h3>Your Wishlist is Empty</h3>
                    <p class="text-muted">Click the heart icon on any product in the marketplace to add it here.</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = this.favorites.map(p => {
            const categoryLabel = CATEGORY_NAMES[p.productCategory] || p.productCategory;
            const inStock = p.stock > 0;
            const fallbackImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';

            return `
                <div class="product-card">
                    <div class="product-img-wrap">
                        <img class="product-img" src="${p.image || fallbackImg}" alt="${p.name}" onerror="this.src='${fallbackImg}'">
                        <span class="product-category-tag">${categoryLabel}</span>
                        <button class="product-fav-btn active" onclick="app.toggleFavorite(${p.id})" title="Remove from Wishlist">
                            <i class="fa-solid fa-heart"></i>
                        </button>
                    </div>
                    <div class="product-info">
                        <h3 class="product-title">${p.name}</h3>
                        <div class="product-stock-line">
                            <span class="stock-dot ${inStock ? 'dot-green' : 'dot-red'}"></span>
                            <span class="${inStock ? 'stock-in' : 'stock-out'}">
                                ${inStock ? `In Stock: ${p.stock} units` : 'Out of stock'}
                            </span>
                        </div>
                        <div class="product-footer">
                            <div class="product-price">$${Number(p.price).toFixed(2)}</div>
                            <div class="product-actions">
                                <button class="btn btn-accent btn-sm btn-glow" onclick="app.addToBasket(${p.id})">
                                    <i class="fa-solid fa-cart-plus"></i> Add to Cart
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    },

    // ==================== CART / BASKET & PAYMENT ====================
    loadBasket: async function() {
        if (!this.user || !this.user.id || this.user.role === 'ROLE_ADMIN') {
            this.basketItems = [];
            this.updateBadges();
            return;
        }
        try {
            const data = await this.api(`/basket/items/${this.user.id}`);
            this.basketItems = Array.isArray(data) ? data : [];
            this.updateBadges();
        } catch (e) {
            this.basketItems = [];
            this.updateBadges();
        }
    },

    addToBasket: async function(productId, count) {
        if (!this.user || !this.user.id) {
            this.toast('Please sign in to add products to your cart', 'info');
            this.openAuthModal('login');
            return;
        }

        const itemCount = count || this.productQtyMap[productId] || 1;

        try {
            await this.api(`/basket/add?userId=${this.user.id}&productId=${productId}&itemCount=${itemCount}`, {
                method: 'POST'
            });
            await this.loadBasket();
            this.toast(`Added ${itemCount} item(s) to your cart 🛒`, 'success');
        } catch (e) {}
    },

    removeFromBasket: async function(productId) {
        if (!this.user || !this.user.id) return;
        try {
            await this.api(`/basket/remove/${this.user.id}/${productId}`, { method: 'DELETE' });
            await this.loadBasket();
            this.renderCartModal();
            this.toast('Item removed from cart', 'info');
        } catch (e) {}
    },

    clearBasket: async function() {
        if (!this.user || !this.user.id) return;
        try {
            await this.api(`/basket/clear/${this.user.id}`, { method: 'DELETE' });
            await this.loadBasket();
            this.renderCartModal();
            this.toast('Shopping cart cleared', 'info');
        } catch (e) {}
    },

    handleCheckout: async function() {
        if (!this.user || !this.user.id) return;
        try {
            const res = await this.api(`/basket/payment/${this.user.id}`, { method: 'POST' });
            this.toast(typeof res === 'string' ? res : 'Order paid and confirmed successfully! 🎉', 'success');
            await this.refreshUserData();
            await this.loadBasket();
            await this.loadProducts();
            this.closeCartModal();
        } catch (e) {}
    },

    openCartModal: async function() {
        if (!this.user || !this.user.id) {
            this.toast('Please sign in to view your shopping cart', 'info');
            this.openAuthModal('login');
            return;
        }
        await this.loadBasket();
        this.renderCartModal();
        document.getElementById('cart-modal').classList.add('open');
    },

    closeCartModal: function() {
        document.getElementById('cart-modal').classList.remove('open');
    },

    renderCartModal: function() {
        const body = document.getElementById('cart-modal-body');
        const footer = document.getElementById('cart-modal-footer');
        if (!body || !footer) return;

        if (this.basketItems.length === 0) {
            body.innerHTML = `
                <div class="empty-state">
                    <i class="fa-solid fa-cart-shopping"></i>
                    <h3>Your cart is empty</h3>
                    <p class="text-muted">Explore items in the marketplace to add them to your cart.</p>
                </div>
            `;
            footer.innerHTML = `
                <button class="btn btn-outline btn-block" onclick="app.closeCartModal()">Continue Shopping</button>
            `;
            return;
        }

        let totalPrice = 0;
        const fallbackImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';

        body.innerHTML = `
            <div class="cart-items-list">
                ${this.basketItems.map(item => {
                    const p = item.product;
                    if (!p) return '';
                    const itemTotal = (p.price || 0) * (item.itemCount || 1);
                    totalPrice += itemTotal;

                    return `
                        <div class="cart-item">
                            <img class="cart-item-img" src="${p.image || fallbackImg}" alt="${p.name}" onerror="this.src='${fallbackImg}'">
                            <div class="cart-item-info">
                                <div class="cart-item-title">${p.name}</div>
                                <div class="cart-item-price">$${Number(p.price).toFixed(2)} × ${item.itemCount} = <b>$${itemTotal.toFixed(2)}</b></div>
                            </div>
                            <button class="btn btn-danger btn-sm" onclick="app.removeFromBasket(${p.id})" title="Remove item">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    `;
                }).join('')}
            </div>
        `;

        const userBalance = Number(this.user.balance || 0);
        const hasEnoughBalance = userBalance >= totalPrice;

        footer.innerHTML = `
            <div class="cart-summary">
                <div class="summary-row">
                    <span>Your Wallet Balance:</span>
                    <span><b style="color: var(--bullish);">$${userBalance.toFixed(2)}</b></span>
                </div>
                <div class="summary-row total">
                    <span>Total Amount:</span>
                    <span style="color: var(--primary); font-size: 20px; font-weight: 800;">$${totalPrice.toFixed(2)}</span>
                </div>
                ${!hasEnoughBalance ? `
                    <div style="color: var(--bearish); font-size: 13px; font-weight: 700; margin-top: 4px;">
                        <i class="fa-solid fa-circle-exclamation"></i> Insufficient balance! Please top up your wallet to proceed with checkout.
                    </div>
                ` : ''}
            </div>
            <div style="display: flex; gap: 10px; margin-top: 14px;">
                <button class="btn btn-outline" onclick="app.clearBasket()" style="flex: 1;">
                    <i class="fa-solid fa-trash"></i> Clear Cart
                </button>
                ${hasEnoughBalance ? `
                    <button class="btn btn-accent btn-lg btn-glow" onclick="app.handleCheckout()" style="flex: 2;">
                        <i class="fa-solid fa-credit-card"></i> Pay & Checkout ($${totalPrice.toFixed(2)})
                    </button>
                ` : `
                    <button class="btn btn-accent btn-lg btn-glow" onclick="app.closeCartModal(); app.openDepositModal();" style="flex: 2;">
                        <i class="fa-solid fa-wallet"></i> Top Up Balance
                    </button>
                `}
            </div>
        `;
    },

    // ==================== ADMIN MANAGEMENT ====================
    switchAdminSubtab: function(subtab) {
        this.adminCurrentSubtab = subtab;
        const prodNavBtn = document.getElementById('admin-nav-products-btn');
        const userNavBtn = document.getElementById('admin-nav-users-btn');
        const prodPanel = document.getElementById('admin-subtab-products');
        const userPanel = document.getElementById('admin-subtab-users');
        const addProdBtn = document.getElementById('admin-add-prod-btn');
        const addUserBtn = document.getElementById('admin-add-user-btn');

        if (subtab === 'products') {
            if (prodNavBtn) prodNavBtn.classList.add('active');
            if (userNavBtn) userNavBtn.classList.remove('active');
            if (prodPanel) prodPanel.style.display = 'block';
            if (userPanel) userPanel.style.display = 'none';
            if (addProdBtn) addProdBtn.style.display = 'inline-flex';
            if (addUserBtn) addUserBtn.style.display = 'none';
            this.renderAdminProducts();
        } else {
            if (prodNavBtn) prodNavBtn.classList.remove('active');
            if (userNavBtn) userNavBtn.classList.add('active');
            if (prodPanel) prodPanel.style.display = 'none';
            if (userPanel) userPanel.style.display = 'block';
            if (addProdBtn) addProdBtn.style.display = 'none';
            if (addUserBtn) addUserBtn.style.display = 'inline-flex';
            this.loadAdminUsers();
        }
    },

    loadAdminData: async function() {
        if (!this.user || this.user.role !== 'ROLE_ADMIN') return;
        await this.loadProducts();
        this.renderAdminProducts();
        await this.loadAdminUsers();
    },

    // --- Admin Products Search, Filter, Sort ---
    handleAdminProductSearch: function(val) {
        this.adminProdSearch = val.trim().toLowerCase();
        this.renderAdminProducts();
    },

    handleAdminProductCategory: function(category) {
        this.adminProdCategory = category;
        this.renderAdminProducts();
    },

    handleAdminProductSort: function(sort) {
        this.adminProdSort = sort;
        this.renderAdminProducts();
    },

    getAdminFilteredProducts: function() {
        let list = [...this.products];

        if (this.adminProdCategory !== 'ALL') {
            list = list.filter(p => p.productCategory === this.adminProdCategory);
        }

        if (this.adminProdSearch) {
            list = list.filter(p => (p.name && p.name.toLowerCase().includes(this.adminProdSearch)) ||
                                     (p.productCategory && p.productCategory.toLowerCase().includes(this.adminProdSearch)));
        }

        if (this.adminProdSort === 'price-asc') {
            list.sort((a, b) => (Number(a.price) || 0) - (Number(b.price) || 0));
        } else if (this.adminProdSort === 'price-desc') {
            list.sort((a, b) => (Number(b.price) || 0) - (Number(a.price) || 0));
        } else if (this.adminProdSort === 'name-asc') {
            list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
        } else if (this.adminProdSort === 'name-desc') {
            list.sort((a, b) => (b.name || '').localeCompare(a.name || ''));
        } else if (this.adminProdSort === 'stock-desc') {
            list.sort((a, b) => (b.stock || 0) - (a.stock || 0));
        } else if (this.adminProdSort === 'stock-asc') {
            list.sort((a, b) => (a.stock || 0) - (b.stock || 0));
        }

        return list;
    },

    renderAdminProducts: function() {
        const tbody = document.getElementById('admin-products-table-body');
        const countBadge = document.getElementById('admin-prod-count');
        if (!tbody) return;

        const filtered = this.getAdminFilteredProducts();
        if (countBadge) countBadge.innerText = filtered.length;

        if (filtered.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center" style="padding: 36px; color: var(--slate-400);">
                        <i class="fa-solid fa-box-open" style="font-size: 24px; margin-bottom: 8px; display: block;"></i>
                        No matching products found
                    </td>
                </tr>
            `;
            return;
        }

        const fallbackImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=500';

        tbody.innerHTML = filtered.map(p => {
            const categoryLabel = CATEGORY_NAMES[p.productCategory] || p.productCategory;
            const inStock = p.stock > 0;
            return `
                <tr>
                    <td><b style="color: var(--slate-500);">#${p.id}</b></td>
                    <td><img src="${p.image || fallbackImg}" class="table-thumb" onerror="this.src='${fallbackImg}'"></td>
                    <td><b>${p.name}</b></td>
                    <td><span class="badge" style="background: var(--primary-light); color: var(--primary); border: 1px solid var(--primary-border);">${categoryLabel}</span></td>
                    <td><b style="color: var(--slate-900);">$${Number(p.price).toFixed(2)}</b></td>
                    <td>
                        <span class="stock-dot ${inStock ? 'dot-green' : 'dot-red'}"></span>
                        <b>${p.stock}</b> units
                    </td>
                    <td style="text-align: right;">
                        <button class="btn btn-outline btn-sm" onclick='app.openEditProductModal(${JSON.stringify(p).replace(/'/g, "&apos;")})' title="Edit product">
                            <i class="fa-solid fa-pen-to-square"></i> Edit
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="app.deleteProduct(${p.id})" title="Delete product">
                            <i class="fa-solid fa-trash"></i> Delete
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    },

    openAddProductModal: function() {
        document.getElementById('product-modal-title').innerText = 'Add New Product';
        document.getElementById('prod-edit-id').value = '';
        document.getElementById('prod-name').value = '';
        document.getElementById('prod-category').value = 'DAIRY_PRODUCTS';
        document.getElementById('prod-price').value = '';
        document.getElementById('prod-stock').value = '50';
        document.getElementById('prod-image').value = '';
        document.getElementById('prod-img-preview').style.display = 'none';
        document.getElementById('product-modal').classList.add('open');
    },

    openEditProductModal: function(product) {
        document.getElementById('product-modal-title').innerText = 'Edit Catalog Product';
        document.getElementById('prod-edit-id').value = product.id;
        document.getElementById('prod-name').value = product.name;
        document.getElementById('prod-category').value = product.productCategory;
        document.getElementById('prod-price').value = product.price;
        document.getElementById('prod-stock').value = product.stock;
        document.getElementById('prod-image').value = product.image || '';
        this.previewProductImage(product.image);
        document.getElementById('product-modal').classList.add('open');
    },

    previewProductImage: function(url) {
        const preview = document.getElementById('prod-img-preview');
        if (url && url.trim()) {
            preview.src = url;
            preview.style.display = 'inline-block';
        } else {
            preview.style.display = 'none';
        }
    },

    closeProductModal: function() {
        document.getElementById('product-modal').classList.remove('open');
    },

    handleSaveProduct: async function(e) {
        e.preventDefault();
        const editId = document.getElementById('prod-edit-id').value;
        const payload = {
            name: document.getElementById('prod-name').value.trim(),
            productCategory: document.getElementById('prod-category').value,
            price: parseFloat(document.getElementById('prod-price').value),
            stock: parseInt(document.getElementById('prod-stock').value),
            image: document.getElementById('prod-image').value.trim()
        };

        try {
            if (editId) {
                await this.api(`/products/${editId}`, {
                    method: 'PUT',
                    body: JSON.stringify(payload)
                });
                this.toast('Product updated successfully! ✅', 'success');
            } else {
                await this.api('/products', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                this.toast('New product added to inventory! ✅', 'success');
            }

            this.closeProductModal();
            await this.loadProducts();
            this.renderAdminProducts();
        } catch (e) {}
    },

    deleteProduct: async function(id) {
        if (!confirm('Are you sure you want to delete this product?')) return;
        try {
            await this.api(`/products/${id}`, { method: 'DELETE' });
            this.toast('Product deleted from catalog', 'info');
            await this.loadProducts();
            this.renderAdminProducts();
        } catch (e) {}
    },

    // --- Admin Users Search, Filter, Sort & CRUD ---
    loadAdminUsers: async function() {
        const tbody = document.getElementById('admin-users-table-body');
        const countBadge = document.getElementById('admin-user-count');
        if (!tbody) return;

        try {
            const users = await this.api('/users');
            this.usersList = Array.isArray(users) ? users : [];
            this.renderAdminUsers();
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding: 24px; color: var(--slate-400);">Unable to load registered users</td></tr>`;
        }
    },

    handleAdminUserSearch: function(val) {
        this.adminUserSearch = val.trim().toLowerCase();
        this.renderAdminUsers();
    },

    handleAdminUserSort: function(sort) {
        this.adminUserSort = sort;
        this.renderAdminUsers();
    },

    getAdminFilteredUsers: function() {
        let list = [...this.usersList];

        if (this.adminUserSearch) {
            list = list.filter(u => {
                const fullName = `${u.name || ''} ${u.surname || ''}`.toLowerCase();
                const email = (u.email || '').toLowerCase();
                const phone = (u.phoneNumber || '').toLowerCase();
                return fullName.includes(this.adminUserSearch) || email.includes(this.adminUserSearch) || phone.includes(this.adminUserSearch);
            });
        }

        if (this.adminUserSort === 'name-asc') {
            list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
        } else if (this.adminUserSort === 'name-desc') {
            list.sort((a, b) => (b.name || '').localeCompare(a.name || ''));
        } else if (this.adminUserSort === 'balance-desc') {
            list.sort((a, b) => (Number(b.balance) || 0) - (Number(a.balance) || 0));
        } else if (this.adminUserSort === 'balance-asc') {
            list.sort((a, b) => (Number(a.balance) || 0) - (Number(b.balance) || 0));
        }

        return list;
    },

    renderAdminUsers: function() {
        const tbody = document.getElementById('admin-users-table-body');
        const countBadge = document.getElementById('admin-user-count');
        if (!tbody) return;

        const filtered = this.getAdminFilteredUsers();
        if (countBadge) countBadge.innerText = filtered.length;

        if (filtered.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center" style="padding: 36px; color: var(--slate-400);">
                        <i class="fa-solid fa-users-slash" style="font-size: 24px; margin-bottom: 8px; display: block;"></i>
                        No matching users found
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = filtered.map(u => `
            <tr>
                <td><b style="color: var(--slate-500);">#${u.id}</b></td>
                <td><b>${u.name} ${u.surname || ''}</b></td>
                <td>${u.email || '-'}</td>
                <td>${u.phoneNumber || '-'} (${u.age || '-'} yrs)</td>
                <td>
                    <span class="role-pill ${u.role === 'ROLE_ADMIN' ? 'role-admin' : 'role-user'}">
                        ${u.role === 'ROLE_ADMIN' ? '👑 ADMIN' : '👤 CUSTOMER'}
                    </span>
                </td>
                <td><b style="color: var(--bullish);">$${Number(u.balance || 0).toFixed(2)}</b></td>
                <td style="text-align: right;">
                    <button class="btn btn-outline btn-sm" onclick='app.openEditUserModal(${JSON.stringify(u).replace(/'/g, "&apos;")})' title="Edit user profile">
                        <i class="fa-solid fa-user-pen"></i> Edit
                    </button>
                    ${u.role !== 'ROLE_ADMIN' ? `
                        <button class="btn btn-danger btn-sm" onclick="app.deleteUser(${u.id})" title="Delete user">
                            <i class="fa-solid fa-trash"></i> Delete
                        </button>
                    ` : ''}
                </td>
            </tr>
        `).join('');
    },

    openAddUserModal: function() {
        document.getElementById('user-modal-title').innerText = 'Add New Customer Account';
        document.getElementById('user-edit-id').value = '';
        document.getElementById('user-edit-name').value = '';
        document.getElementById('user-edit-surname').value = '';
        document.getElementById('user-edit-age').value = '';
        document.getElementById('user-edit-phone').value = '';
        document.getElementById('user-edit-email').value = '';
        document.getElementById('user-edit-balance').value = '1000';
        document.getElementById('user-edit-password').value = '';
        document.getElementById('user-modal').classList.add('open');
    },

    openEditUserModal: function(user) {
        document.getElementById('user-modal-title').innerText = 'Edit User Profile';
        document.getElementById('user-edit-id').value = user.id;
        document.getElementById('user-edit-name').value = user.name || '';
        document.getElementById('user-edit-surname').value = user.surname || '';
        document.getElementById('user-edit-age').value = user.age || '';
        document.getElementById('user-edit-phone').value = user.phoneNumber || '';
        document.getElementById('user-edit-email').value = user.email || '';
        document.getElementById('user-edit-balance').value = user.balance || 0;
        document.getElementById('user-edit-password').value = '';
        document.getElementById('user-modal').classList.add('open');
    },

    closeUserModal: function() {
        document.getElementById('user-modal').classList.remove('open');
    },

    handleSaveUser: async function(e) {
        e.preventDefault();
        const editId = document.getElementById('user-edit-id').value;
        const payload = {
            name: document.getElementById('user-edit-name').value.trim(),
            surname: document.getElementById('user-edit-surname').value.trim(),
            age: document.getElementById('user-edit-age').value.trim(),
            phoneNumber: document.getElementById('user-edit-phone').value.trim(),
            email: document.getElementById('user-edit-email').value.trim(),
            balance: parseFloat(document.getElementById('user-edit-balance').value) || 0
        };

        const password = document.getElementById('user-edit-password').value;
        if (password && password.trim()) {
            payload.password = password.trim();
        }

        try {
            if (editId) {
                await this.api(`/users/${editId}`, {
                    method: 'PUT',
                    body: JSON.stringify(payload)
                });
                this.toast('User profile updated successfully! ✅', 'success');
            } else {
                await this.api('/users', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                this.toast('New user registered successfully! ✅', 'success');
            }

            this.closeUserModal();
            await this.loadAdminUsers();
        } catch (e) {}
    },

    deleteUser: async function(id) {
        if (!confirm('Are you sure you want to delete this user account?')) return;
        try {
            await this.api(`/users/${id}`, { method: 'DELETE' });
            this.toast('User account removed', 'info');
            await this.loadAdminUsers();
        } catch (e) {}
    },

    // ==================== NAVIGATION & MODALS ====================
    showTab: function(tabName) {
        // Hide all tabs
        document.querySelectorAll('.tab-content').forEach(el => el.style.display = 'none');
        document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));

        const targetTab = document.getElementById(`tab-${tabName}`);
        const targetBtn = document.getElementById(`tab-btn-${tabName}`);

        if (targetTab) targetTab.style.display = 'block';
        if (targetBtn) targetBtn.classList.add('active');

        if (tabName === 'favorites') {
            this.loadFavorites();
        } else if (tabName === 'admin') {
            this.loadAdminData();
        } else if (tabName === 'market') {
            this.renderProducts();
        }
    },

    openAuthModal: function(mode = 'login') {
        this.switchAuthMode(mode);
        document.getElementById('auth-modal').classList.add('open');
    },

    closeAuthModal: function() {
        document.getElementById('auth-modal').classList.remove('open');
    },

    switchAuthMode: function(mode) {
        const loginForm = document.getElementById('login-form');
        const regForm = document.getElementById('register-form');
        const loginTab = document.getElementById('auth-tab-login');
        const regTab = document.getElementById('auth-tab-register');
        const title = document.getElementById('auth-modal-title');

        if (mode === 'login') {
            loginForm.style.display = 'block';
            regForm.style.display = 'none';
            loginTab.classList.add('active');
            regTab.classList.remove('active');
            title.innerText = 'Sign In';
        } else {
            loginForm.style.display = 'none';
            regForm.style.display = 'block';
            loginTab.classList.remove('active');
            regTab.classList.add('active');
            title.innerText = 'Create Account';
        }
    },

    openDepositModal: function() {
        if (!this.user) {
            this.openAuthModal('login');
            return;
        }
        document.getElementById('deposit-modal').classList.add('open');
    },

    closeDepositModal: function() {
        document.getElementById('deposit-modal').classList.remove('open');
    },

    updateBadges: function() {
        const favBadge = document.getElementById('fav-count-badge');
        const cartBadge = document.getElementById('cart-count-badge');
        if (favBadge) favBadge.innerText = this.favorites.length;
        if (cartBadge) {
            const count = this.basketItems.reduce((acc, item) => acc + (item.itemCount || 1), 0);
            cartBadge.innerText = count;
        }
    },

    renderNavAuth: function() {
        const container = document.getElementById('nav-auth-section');
        const demoBanner = document.getElementById('demo-banner');
        const adminBtn = document.getElementById('tab-btn-admin');
        const favBtn = document.getElementById('tab-btn-favorites');
        const cartBtn = document.getElementById('tab-btn-basket');
        const marketBtn = document.getElementById('tab-btn-market');
        const globalSearch = document.getElementById('global-nav-search');

        if (!container) return;

        if (this.user) {
            if (demoBanner) demoBanner.style.display = 'none';
            const isAdmin = this.user.role === 'ROLE_ADMIN';

            if (isAdmin) {
                // ADMIN ROLE: NO Wishlist, NO Basket, NO Balance
                if (adminBtn) adminBtn.style.display = 'flex';
                if (favBtn) favBtn.style.display = 'none';
                if (cartBtn) cartBtn.style.display = 'none';
                if (marketBtn) marketBtn.style.display = 'none';
                if (globalSearch) globalSearch.style.display = 'none';

                container.innerHTML = `
                    <div class="user-profile-card">
                        <div class="user-avatar">${(this.user.name || 'A').charAt(0).toUpperCase()}</div>
                        <div class="user-info-text">
                            <span class="user-name-label">${this.user.name}</span>
                            <span class="role-pill role-admin">👑 ADMIN</span>
                        </div>
                    </div>
                    <button class="btn btn-outline btn-sm" onclick="app.logout()" title="Sign Out">
                        <i class="fa-solid fa-arrow-right-from-bracket"></i> Sign Out
                    </button>
                `;
            } else {
                // USER ROLE: Show Wishlist, Basket, Balance
                if (adminBtn) adminBtn.style.display = 'none';
                if (favBtn) favBtn.style.display = 'flex';
                if (cartBtn) cartBtn.style.display = 'flex';
                if (marketBtn) marketBtn.style.display = 'flex';
                if (globalSearch) globalSearch.style.display = 'flex';

                container.innerHTML = `
                    <div class="user-profile-card">
                        <div class="user-avatar">${(this.user.name || 'U').charAt(0).toUpperCase()}</div>
                        <div class="user-info-text">
                            <span class="user-name-label">${this.user.name}</span>
                            <span class="role-pill role-user">👤 CUSTOMER</span>
                        </div>
                    </div>
                    <div class="user-wallet-btn" onclick="app.openDepositModal()" title="Click to deposit funds">
                        <i class="fa-solid fa-wallet"></i>
                        <span>$${Number(this.user.balance || 0).toFixed(2)}</span>
                        <i class="fa-solid fa-circle-plus" style="font-size: 12px; color: var(--bullish);"></i>
                    </div>
                    <button class="btn btn-outline btn-sm" onclick="app.logout()" title="Sign Out">
                        <i class="fa-solid fa-arrow-right-from-bracket"></i>
                    </button>
                `;
            }
        } else {
            // GUEST / VISITOR
            if (demoBanner) demoBanner.style.display = 'block';
            if (adminBtn) adminBtn.style.display = 'none';
            if (favBtn) favBtn.style.display = 'flex';
            if (cartBtn) cartBtn.style.display = 'flex';
            if (marketBtn) marketBtn.style.display = 'flex';
            if (globalSearch) globalSearch.style.display = 'flex';

            container.innerHTML = `
                <button class="btn btn-outline btn-sm" onclick="app.openAuthModal('login')">
                    <i class="fa-solid fa-arrow-right-to-bracket"></i> Sign In
                </button>
                <button class="btn btn-primary btn-sm btn-glow" onclick="app.openAuthModal('register')">
                    <i class="fa-solid fa-user-plus"></i> Sign Up
                </button>
            `;
        }
    },

    toast: function(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        let icon = 'fa-info-circle';
        if (type === 'success') icon = 'fa-circle-check text-success';
        if (type === 'error') icon = 'fa-circle-exclamation text-danger';

        toast.innerHTML = `
            <i class="fa-solid ${icon}"></i>
            <span>${message}</span>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3500);
    }
};

// Launch application on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    app.init();
});
