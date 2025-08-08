FROM node:18

# 작업 디렉토리
WORKDIR /app

# translation-server 설치
RUN git clone --recursive https://github.com/zotero/translation-server.git .
RUN npm install

# 서버 포트 환경변수
ENV PORT=1969

# 포트 오픈
EXPOSE 1969

# 서버 실행
CMD ["npm", "start"]
