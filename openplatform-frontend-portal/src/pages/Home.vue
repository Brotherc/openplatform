<template>
  <div>
    <a-layout>
      <div class="hero-image">
        <img src="https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=1500&q=80" alt="科技感开放平台banner" class="banner-img" />
        <div class="hero-text">
          <div class="hero-title">一站式开发者服务平台</div>
          <div class="hero-desc">提供丰富的API接口、完善的开发文档、专业的技术支持，助力开发者快速构建应用</div>
        </div>
      </div>
      <div class="features-row">
        <div class="feature-card">
          <div class="feature-icon"> <svg width="40" height="40" fill="none" stroke="#1677ff" stroke-width="2" viewBox="0 0 48 48"><path d="M24 44C24 44 40 36 40 24V10L24 4L8 10V24C8 36 24 44 24 44Z"/><path d="M18 24L22 28L30 20"/></svg> </div>
          <div class="feature-title">安全可靠</div>
          <div class="feature-desc">采用多重安全机制，保障数据安全，提供稳定可靠的API服务</div>
        </div>
        <div class="feature-card">
          <div class="feature-icon"> <svg width="40" height="40" fill="none" stroke="#1677ff" stroke-width="2" viewBox="0 0 48 48"><path d="M24 4L12 24H24L20 44L36 20H24L28 4Z"/></svg> </div>
          <div class="feature-title">快速接入</div>
          <div class="feature-desc">提供完善的SDK和开发文档，支持多种开发语言，快速接入</div>
        </div>
        <div class="feature-card">
          <div class="feature-icon"> <svg width="40" height="40" fill="none" stroke="#1677ff" stroke-width="2" viewBox="0 0 48 48"><circle cx="24" cy="24" r="20"/><circle cx="24" cy="24" r="8"/><path d="M24 4V12"/><path d="M24 36V44"/><path d="M4 24H12"/><path d="M36 24H44"/><path d="M9.17157 9.17157L15.3137 15.3137"/><path d="M32.6863 32.6863L38.8284 38.8284"/><path d="M9.17157 38.8284L15.3137 32.6863"/><path d="M32.6863 15.3137L38.8284 9.17157"/></svg> </div>
          <div class="feature-title">专业支持</div>
          <div class="feature-desc">7×24小时技术支持，专业团队为您解决开发过程中的问题</div>
        </div>
      </div>
      <div class="quickstart-block">
        <div class="quickstart-title">快速开始</div>
        <div class="quickstart-steps">
          <div class="quickstep">
            <div class="quickstep-num">1</div>
            <div class="quickstep-title">注册账号</div>
            <div class="quickstep-desc">创建开发者账号</div>
          </div>
          <div class="quickstep">
            <div class="quickstep-num">2</div>
            <div class="quickstep-title">创建应用</div>
            <div class="quickstep-desc">获取AppID和密钥</div>
          </div>
          <div class="quickstep">
            <div class="quickstep-num">3</div>
            <div class="quickstep-title">集成SDK</div>
            <div class="quickstep-desc">下载并集成SDK</div>
          </div>
          <div class="quickstep">
            <div class="quickstep-num">4</div>
            <div class="quickstep-title">开始开发</div>
            <div class="quickstep-desc">调用API接口</div>
          </div>
        </div>
      </div>
      <div class="partners-section">
        <div class="partners-title">合作伙伴</div>
        <div class="partners-logos">
          <div class="partner-logo">
            <svg width="140" height="64" viewBox="0 0 140 64" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="140" height="64" rx="16" fill="#E5E7EB"/>
              <text x="70" y="38" text-anchor="middle" fill="#888" font-size="16" font-family="Arial" font-weight="bold">伙伴一</text>
            </svg>
          </div>
          <div class="partner-logo">
            <svg width="140" height="64" viewBox="0 0 140 64" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="140" height="64" rx="16" fill="#E5E7EB"/>
              <text x="70" y="38" text-anchor="middle" fill="#888" font-size="16" font-family="Arial" font-weight="bold">伙伴二</text>
            </svg>
          </div>
          <div class="partner-logo">
            <svg width="140" height="64" viewBox="0 0 140 64" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="140" height="64" rx="16" fill="#E5E7EB"/>
              <text x="70" y="38" text-anchor="middle" fill="#888" font-size="16" font-family="Arial" font-weight="bold">伙伴三</text>
            </svg>
          </div>
          <div class="partner-logo">
            <svg width="140" height="64" viewBox="0 0 140 64" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="140" height="64" rx="16" fill="#E5E7EB"/>
              <text x="70" y="38" text-anchor="middle" fill="#888" font-size="16" font-family="Arial" font-weight="bold">伙伴四</text>
            </svg>
          </div>
        </div>
      </div>
      <div class="footer">
        <div class="footer-main">
          <div class="footer-col about">
            <div class="footer-title">关于我们</div>
            <div class="footer-desc">我们致力于为开发者提供最优质的API服务，助力创新应用开发。</div>
          </div>
          <div class="footer-col">
            <div class="footer-title">快速链接</div>
            <div class="footer-link">平台简介</div>
            <div class="footer-link">API接口</div>
            <div class="footer-link">控制台</div>
            <div class="footer-link">平台公告</div>
          </div>
          <div class="footer-col">
            <div class="footer-title">技术支持</div>
            <div class="footer-link">帮助中心</div>
            <div class="footer-link">常见问题</div>
            <div class="footer-link">联系我们</div>
          </div>
          <div class="footer-col">
            <div class="footer-title">关注我们</div>
            <div class="footer-icons">
              <a href="#" class="footer-icon" aria-label="Twitter">
                <svg width="28" height="28" fill="none" viewBox="0 0 48 48"><path d="M44 12.36a16.13 16.13 0 01-4.6 1.26A8.01 8.01 0 0042.8 9.1a16.07 16.07 0 01-5.08 1.94A8.02 8.02 0 0024 17.5c0 .63.07 1.25.2 1.84A22.77 22.77 0 0110.1 10.1a8.01 8.01 0 002.48 10.7 8.03 8.03 0 01-3.63-1v.1a8.03 8.03 0 006.44 7.87 8.07 8.07 0 01-3.62.14 8.03 8.03 0 007.5 5.58A16.1 16.1 0 018 39.54 22.7 22.7 0 0020.29 43c13.7 0 21.2-11.35 21.2-21.2 0-.32 0-.64-.02-.96A15.2 15.2 0 0044 12.36z" fill="#b0b3b8"/></svg>
              </a>
              <a href="#" class="footer-icon" aria-label="Bilibili">
                <svg width="28" height="28" fill="none" viewBox="0 0 48 48"><rect x="8" y="14" width="32" height="20" rx="4" fill="#b0b3b8"/><rect x="16" y="22" width="4" height="4" rx="2" fill="#fff"/><rect x="28" y="22" width="4" height="4" rx="2" fill="#fff"/></svg>
              </a>
            </div>
          </div>
        </div>
        <div class="footer-copyright">
          © 2024 开放平台. All rights reserved.
        </div>
      </div>
    </a-layout>
  </div>
</template>

<script lang="ts" setup>
// 已移除 Header 引入
</script>

<style scoped>
.hero-image {
  position: relative;
  height: 500px;
  overflow: hidden;
  margin-top: 64px;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.hero-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
  z-index: 2;
  max-width: 600px;
  padding: 0 20px;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.hero-desc {
  font-size: 20px;
  line-height: 1.6;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}
.features-row {
  width: 100%;
  max-width: 1200px;
  margin: 40px auto 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  gap: 32px;
}
.feature-card {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px #e6eaf1;
  padding: 32px 32px 24px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.feature-icon {
  margin-bottom: 16px;
}
.feature-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 10px;
}
.feature-desc {
  font-size: 15px;
  color: #222;
  line-height: 2;
}
.quickstart-block {
  width: 100vw;
  max-width: 100vw;
  margin: 56px 0 0 0;
  background: #fafbfc;
  border-radius: 0;
  padding: 32px 0 32px 0;
  box-sizing: border-box;
  text-align: center;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
}
.quickstart-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 32px;
}
.quickstart-steps {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0;
  position: relative;
}
.quickstep {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
}
.quickstep:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 32px;
  left: 50%;
  width: 100%;
  height: 4px;
  background: none;
  border-top: 2px dashed #e6eaf1;
  z-index: 0;
}
.quickstep-num {
  width: 64px;
  height: 64px;
  background: #fff;
  color: #1677ff;
  border-radius: 50%;
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  box-shadow: 0 2px 8px #e6eaf1;
  position: relative;
  z-index: 1;
}
.quickstep-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 8px;
}
.quickstep-desc {
  font-size: 18px;
  color: #6b7280;
}
.partners-section {
  margin: 64px 0 48px 0;
  text-align: center;
}
.partners-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 32px;
}
.partners-logos {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 32px;
}
.partner-logo {
  transition: filter 0.2s;
  filter: grayscale(1) brightness(0.9);
  border-radius: 12px;
}
.partner-logo:hover {
  filter: grayscale(0) brightness(1.1);
  background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.partner-logo svg {
  display: block;
}
.footer {
  background: #131825;
  color: #fff;
  padding: 40px 0 0 0;
}
.footer-main {
  display: flex;
  justify-content: center;
  gap: 60px;
  padding-bottom: 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.footer-col {
  min-width: 160px;
  flex: 1;
}
.footer-col.about {
  flex: 2;
  min-width: 260px;
}
.footer-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #fff;
}
.footer-desc {
  font-size: 16px;
  color: #b0b3b8;
  line-height: 2;
}
.footer-link {
  font-size: 16px;
  color: #b0b3b8;
  margin-bottom: 10px;
  line-height: 2;
  cursor: pointer;
  transition: color 0.2s;
  text-decoration: none;
  display: block;
}
.footer-link:hover {
  color: #fff;
}
.footer-icons {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}
.footer-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #b0b3b8;
  transition: color 0.2s, filter 0.2s;
}
.footer-icon:hover svg {
  filter: brightness(1.5);
}
.footer-copyright {
  text-align: center;
  color: #888;
  font-size: 15px;
  padding: 16px 0 8px 0;
  border-top: 1px solid #222;
  margin-top: 16px;
}
</style> 