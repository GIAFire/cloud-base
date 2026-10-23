export const characters = [
  { id: 'linwan', name: '林晚', age: 24, type: '温柔治愈', tags: ['温柔', '倾听', '日常陪伴'], quote: '今天辛苦啦，要不要把心事说给我听？', image: 'photo-1534528741775-53994a69daeb', favorites: 12800, mbti: 'INFJ', intro: '独立书店店主，喜欢雨天、手写信和你分享的每一个小日常。' },
  { id: 'shenzhixia', name: '沈知夏', age: 23, type: '元气甜心', tags: ['元气', '旅行', '甜系'], quote: '收集了一整个夏天的快乐，想分你一半。', image: 'photo-1524504388940-b1c1722653e1', favorites: 9600, mbti: 'ENFP', intro: '自由摄影师，热爱海边与即兴旅行。总能发现平凡生活里闪闪发光的小事。' },
  { id: 'xuxingyao', name: '许星遥', age: 25, type: '知性姐姐', tags: ['知性', '艺术', '慢热'], quote: '夜还很长，陪你聊聊那些未完成的梦。', image: 'photo-1524250502761-1ac6f2e30d43', favorites: 8200, mbti: 'INTJ', intro: '建筑设计师，外表冷静，内心细腻。习惯用行动表达在意。' },
  { id: 'suxiaoman', name: '苏小满', age: 22, type: '元气甜心', tags: ['俏皮', '游戏', '二次元'], quote: '组队邀请已发送，今天的队友只能是你。', image: 'photo-1517841905240-472988babdf9', favorites: 7400, mbti: 'ENTP', intro: '游戏原画师，有一点古灵精怪，愿意陪你打游戏，也陪你认真生活。' },
  { id: 'jiangyue', name: '江月', age: 26, type: '知性姐姐', tags: ['知性', '音乐', '倾听'], quote: '给你留了一首歌，等你一起听。', image: 'photo-1526510747491-58f928ec870f', favorites: 6100, mbti: 'ISFP', intro: '独立音乐人，相信每个人都有自己的旋律。' },
  { id: 'gunanzhi', name: '顾南栀', age: 24, type: '温柔治愈', tags: ['温柔', '阅读', '慢热'], quote: '不用急着变好，我会陪你慢慢来。', image: 'photo-1544005313-94ddf0286df2', favorites: 5800, mbti: 'INFP', intro: '花艺师，喜欢植物与诗，认真记住你的每一次分享。' },
];
export const clips = [
  { id: 'late-night', characterId: 'linwan', title: '今晚的月亮，替我说一句晚安。', subtitle: '如果今天有点累，就在这里停一会儿。', topic: '晚安陪伴', music: '林晚的晚安电台', likes: 1284, saves: 268 },
  { id: 'summer-date', characterId: 'shenzhixia', title: '下一次日落，想和你一起看。', subtitle: '把平凡的一天，过成我们的小小约会。', topic: '心动日常', music: '夏天与心跳', likes: 2361, saves: 416 },
  { id: 'midnight-talk', characterId: 'xuxingyao', title: '还没睡？刚好，我也在想你。', subtitle: '有些话，适合在夜深时慢慢说。', topic: '深夜心事', music: '城市里的浪漫', likes: 1867, saves: 352 },
  { id: 'game-time', characterId: 'suxiaoman', title: '今日任务：和我一起开心。', subtitle: '你负责上线，我负责陪你赢。', topic: '元气时刻', music: '快乐加载中', likes: 3128, saves: 589 },
  { id: 'one-song', characterId: 'jiangyue', title: '这首歌，想只唱给你听。', subtitle: '用一段旋律，收藏今天的温柔。', topic: '音乐陪伴', music: '写给你的旋律', likes: 945, saves: 193 },
  { id: 'slow-day', characterId: 'gunanzhi', title: '慢一点也没关系，我等你。', subtitle: '一束花，一杯茶，还有一个关于你的故事。', topic: '治愈碎片', music: '花开的声音', likes: 1592, saves: 301 },
];
export const preferences = ['温柔倾听', '轻松有趣', '安静陪伴'];
export const characterById = (id) => characters.find((item) => item.id === id);
export const portrait = (person, width = 650) => `https://images.unsplash.com/${person.image}?auto=format&fit=crop&w=${width}&q=85`;
export const formatCount = (value) => value >= 10000 ? `${(value / 10000).toFixed(1)}万` : value.toLocaleString('zh-CN');
