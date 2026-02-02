import { defineConfig } from 'cypress'

export default defineConfig({
  
  e2e: {
    //'baseUrl': 'http://localhost:4200'
    'baseUrl': 'https://localhost:54545'
  },
  
  
  component: {
    devServer: {
      framework: 'angular',
      bundler: 'webpack',
    },
    specPattern: '**/*.cy.ts'
  }
  
})