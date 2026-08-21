export function getAccessToken() {
  return uni.getStorageSync('token');
}

export function getRefreshToken() {
  return uni.getStorageSync('refresh-token');
}

export function setToken(accessToken, refreshToken) {
  uni.setStorageSync('token', accessToken);
  uni.setStorageSync('refresh-token', refreshToken);
}

export function clearToken() {
  uni.removeStorageSync('token');
  uni.removeStorageSync('refresh-token');
}
