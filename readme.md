# 🔌 Socket Presence Server

This is a lightweight **Websocket server** that tracks and emits **user online/offline events** in real time. It helps other services or frontends react instantly to presence changes.

## 📦 Features

- Emits `user:online` and `user:offline` events
- Identifies users by JWT or user ID on connection
- Integrates easily with Redis (optional) for distributed deployments

## Consumer Service
[User-Service](https://github.com/Mukulphougat/user-presence-service)
