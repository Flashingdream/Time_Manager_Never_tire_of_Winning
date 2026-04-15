// src/notification.js
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient = null;

export function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    onConnect: (frame) => {
      console.log('Connected: ' + frame);
      stompClient.subscribe('/topic/reminders', (message) => {
        showNotification(message.body);
      });
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    }
  });

  stompClient.activate();
}

export function disconnectWebSocket() {
  if (stompClient !== null) {
    stompClient.deactivate();
  }
  console.log("Disconnected");
}

export function requestNotificationPermission() {
  if ('Notification' in window) {
    Notification.requestPermission().then(function (permission) {
      if (permission === 'granted') {
        console.log('Notification permission granted.');
      } else {
        console.log('Notification permission denied.');
      }
    });
  }
}

export function showNotification(message) {
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification('事件提醒', {
      body: message,
      icon: '/favicon.ico' // 可以替换为合适的图标
    });
  } else {
    alert(message); // 如果不支持或未授权，使用alert作为后备
  }
}