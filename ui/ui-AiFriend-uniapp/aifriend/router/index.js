const routes = {
  login: '/pages/auth/login',
  register: '/pages/auth/register',
  home: '/pages/home/index',
  card: '/pages/card/index',
  chat: '/pages/chat/index',
  profile: '/pages/profile/index',
};

const tabPages = new Set([routes.home, routes.card, routes.chat, routes.profile]);

function go(url, options = {}) {
  if (tabPages.has(url)) {
    uni.switchTab({ url });
    return;
  }

  if (options.replace) {
    uni.redirectTo({ url });
    return;
  }

  uni.navigateTo({ url });
}

function ensureLogin() {
  if (uni.getStorageSync('token')) return true;
  uni.reLaunch({ url: routes.login });
  return false;
}

export default {
  routes,
  go,
  ensureLogin,
  login: () => uni.reLaunch({ url: routes.login }),
  register: () => go(routes.register),
  home: () => go(routes.home),
};
