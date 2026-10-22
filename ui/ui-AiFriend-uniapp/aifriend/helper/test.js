export function isMobile(value) {
  return /^1[23456789]\d{9}$/.test(value);
}

export function isSmsCode(value) {
  return /^\d{4,6}$/.test(value);
}

export function isEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value?.trim());
}

export function isEmailCode(value) {
  return /^\d{6}$/.test(value);
}

export function isPassword(value) {
  return typeof value === 'string' && value.length >= 8 && value.length <= 32;
}
