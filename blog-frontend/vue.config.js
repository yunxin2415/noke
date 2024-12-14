const { defineConfig } = require('@vue/cli-service')
const path = require('path')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        onProxyReq: function(proxyReq, req, res) {
          console.log('代理请求:', {
            originalUrl: req.originalUrl,
            method: req.method,
            headers: req.headers,
            proxyUrl: proxyReq.path,
            proxyMethod: proxyReq.method,
            proxyHeaders: proxyReq.getHeaders()
          });
        },
        onProxyRes: function(proxyRes, req, res) {
          console.log('代理响应:', {
            statusCode: proxyRes.statusCode,
            headers: proxyRes.headers,
            originalUrl: req.originalUrl
          });
          
          // 如果是错误响应，记录响应体
          if (proxyRes.statusCode >= 400) {
            let body = '';
            proxyRes.on('data', function(chunk) {
              body += chunk;
            });
            proxyRes.on('end', function() {
              try {
                const parsedBody = JSON.parse(body);
                console.log('错误响应体:', parsedBody);
              } catch (e) {
                console.log('原始错误响应:', body);
              }
            });
          }
        }
      }
    },
    client: {
      overlay: {
        errors: true,
        warnings: false
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    performance: Object.assign({}, {
      hints: false,
      maxEntrypointSize: 512000,
      maxAssetSize: 512000
    })
  },
  productionSourceMap: false,
  css: {
    extract: process.env.NODE_ENV === 'production',
    sourceMap: false
  }
})
