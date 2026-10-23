export const preferences = ['温柔倾听', '轻松有趣', '安静陪伴'];

// Image URLs belong to character data supplied by the backend.
export const portrait = (person) => person?.image || '';
export const formatCount = (value = 0) => value >= 10000
  ? `${(value / 10000).toFixed(1)}万` : value.toLocaleString('zh-CN');
