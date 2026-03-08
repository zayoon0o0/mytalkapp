#include "chat.h"
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <stdlib.h>
#include <android/log.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <jni.h>
#include <stdbool.h>
#include "native_bindings.h"

#define SERVER_IP "192.168.1.103"
#define PORT 8080
#define BUF_SIZE 1024
#define USERNAME_LEN 32

int sock = -1;
char name[BUF_SIZE];

const struct Headers HEADERS = {
        .Hello = 1,
        .Acknowledge = 2,
        .Login = 3,
        .ClientList = 4,
        .Message = 5,
        .PrivateMessage = 6,
};
void connect_to_server(const char* username, const uint8_t length) {
    if (sock >= 0) {
        close(sock);
        sock = -1;
    }
    struct sockaddr_in addr;
    size_t copy_len = (length < USERNAME_LEN - 1) ? length : USERNAME_LEN - 1;
    memcpy(name, username, copy_len);
    name[copy_len] = '\0';
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("socket");
        return;
    }
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(PORT);
    addr.sin_addr.s_addr = inet_addr(SERVER_IP);
    if (connect(sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        perror("connect");
        close(sock);
        return;
    }
    uint32_t total_len = htonl(1 + length);
    write(sock, &total_len, 4);
    write(sock, &HEADERS.Login, 1);
    write(sock, username, length);
    uint8_t buffer[1];
    read(sock, buffer, 1);
    if (buffer[0] == HEADERS.Hello) {
        __android_log_print(ANDROID_LOG_INFO, "ChatActivity", "Connected");
    }
    uint8_t ack = HEADERS.Acknowledge;
    send_message(&ack, 1);
}

void send_message(const char* msg, const int length) {
    if (length == 1 && (uint8_t)msg[0] == HEADERS.Acknowledge) {
        write(sock, &HEADERS.Acknowledge, 1);
        return;
    }
    uint32_t total_len = 1 + length;
    if (total_len > BUF_SIZE) {
        fprintf(stderr, "send_message: Message too long\n");
        return;
    }
    uint32_t net_len = htonl(total_len);
    write(sock, &net_len, 4);
    uint8_t sendbuf[BUF_SIZE];
    sendbuf[0] = HEADERS.Message;
    memcpy(sendbuf + 1, msg, length);
    write(sock, sendbuf, total_len);
}
char *list_users_raw() {
    if (sock < 0) {
        fprintf(stderr, "list_users_raw: socket closed\n");
        return NULL;
    }
    uint32_t len = htonl(1);
    write(sock, &len, 4);
    write(sock, &HEADERS.ClientList, 1);
    uint32_t net_len;
    ssize_t n = read(sock, &net_len, 4);
    if (n != 4) {
        perror("list_users_raw: failed to read length");
        return NULL;
    }
    uint32_t length = ntohl(net_len);
    if (length >= BUF_SIZE) {
        fprintf(stderr, "list_users_raw: message too large\n");
        return NULL;
    }
    uint8_t buffer[BUF_SIZE];
    n = read(sock, buffer, length);
    if (n != length) {
        perror("list_users_raw: failed to read full message");
        return NULL;
    }
    if (buffer[0] != HEADERS.ClientList) {
        fprintf(stderr, "list_users_raw: unexpected header %d\n", buffer[0]);
        return NULL;
    }
    int data_len = length - 1;
    char *data = malloc(data_len + 1);
    if (!data) {
        perror("list_users_raw: malloc failed");
        return NULL;
    }
    memcpy(data, buffer + 1, data_len);
    data[data_len] = '\0';
    return data;
}
void send_privitemessage(const char* msg, int length) {
    if (sock < 0) {
        fprintf(stderr, "send_privitemessage called but socket is closed\n");
        return;
    }
    uint32_t total_len = 1 + length;
    if (total_len > BUF_SIZE) {
        fprintf(stderr, "send_privitemessage: Message too long\n");
        return;
    }
    uint32_t net_len = htonl(total_len);
    write(sock, &net_len, 4);
    uint8_t sendbuf[BUF_SIZE];
    sendbuf[0] = HEADERS.PrivateMessage;
    memcpy(sendbuf + 1, msg, length);
    write(sock, sendbuf, total_len);
}
void send_ackmessage() {
    uint8_t sendbuf[BUF_SIZE];
    sendbuf[0] = HEADERS.Acknowledge;
    write(sock, sendbuf, 1);
}
void receive(void (*func)(void *, void *, char *, int), void *arg, void *activity) {
    uint32_t net_len;
    size_t total = 0;
    char *ptr = (char*)&net_len;
    while (total < sizeof(net_len)) {
        ssize_t r = read(sock, ptr + total, sizeof(net_len) - total);
        if (r <= 0) return;
        total += r;
    }
    uint32_t len = ntohl(net_len);
    if (len == 0 || len >= BUF_SIZE) return;
    uint8_t buffer[BUF_SIZE];
    total = 0;
    ptr = (char*)buffer;
    while (total < len) {
        ssize_t r = read(sock, ptr + total, len - total);
        if (r <= 0) return;
        total += r;
    }
    if (buffer[0] == HEADERS.PrivateMessage) {
        const char* prefix = "[Private] ";
        char* msg = (char*)(buffer + 1);
        int msg_len = len - 1;
        int tag_len = strlen(prefix);
        int total_len = tag_len + msg_len + 1;
        char* new_data = malloc(total_len + 1);
        if (!new_data) return;
        memcpy(new_data, prefix, tag_len);
        memcpy(new_data + tag_len, msg, msg_len);
        new_data[total_len] = '\0';
        func(arg, activity, new_data, total_len);
        free(new_data);
        return;
    }
    func(arg, activity, (char*)(buffer + 1), len - 1);
}
void closesock() {
    if (sock >= 0) {
        close(sock);
        sock = -1;
    }
}
