export function isMobile(value) {
  return /^1[23456789]\d{9}$/.test(value);
}

export function isPassword(value) {
  return /^(?=.*[A-Za-z])(?=.*\d)[0-9A-Za-z]{6,12}$/.test(value);
}

export function isSmsCode(value) {
  return /^\d{4,6}$/.test(value);
}
