<script setup>
import { onLaunch } from '@dcloudio/uni-app';
import { useExperienceStore } from '@/aifriend/store/experience';

onLaunch(() => {
  useExperienceStore().initializeAuth();
});
</script>

<style lang="scss">
@use '@/aifriend/styles/index.scss' as *;
</style>
