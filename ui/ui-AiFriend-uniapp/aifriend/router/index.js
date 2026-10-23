const routes = {
    home: '/pages/home/index', conversations: '/pages/conversations/index',
    video: '/pages/video/index', profile: '/pages/profile/index',
    chat: '/pages/chat/index', login: '/pages/auth/login',
    register: '/pages/auth/register', forgotPassword: '/pages/auth/forgot-password',
};

export function currentRoute() {
    const pages = getCurrentPages();
    const current = pages[pages.length - 1];
    if (!current) return routes.home;
    const query = Object.entries(current.options || {}).map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`).join('&');
    return `/${current.route}${query ? `?${query}` : ''}`;
}

function go(url, options = {}) {
    if (currentRoute() === url) return;
    if (options.reset) uni.reLaunch({url});
    else if (options.replace) uni.redirectTo({url});
    else uni.navigateTo({url});
}

export default {routes, go, home: () => go(routes.home, {reset: true})};
