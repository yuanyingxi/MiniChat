<script setup lang="ts">
</script>

<template>
  <router-view />
</template>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: var(--el-font-family);
  color: var(--line-text-primary);
  background: var(--line-bg-primary);
  -webkit-font-smoothing: antialiased;
}

a {
  color: var(--line-green);
  text-decoration: none;
}
a:hover {
  color: var(--line-green-hover);
}

::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #D0D0D0;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #B0B0B0;
}

::selection {
  background: var(--line-green-light);
  color: var(--line-text-primary);
}
</style>
