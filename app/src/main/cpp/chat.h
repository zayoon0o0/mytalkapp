#include <sys/types.h>
#include <jni.h>

#ifndef CHAT_H
#define CHAT_H

struct Headers {
    uint8_t Hello;
    uint8_t Acknowledge;
    uint8_t Login; // username : password \0
    uint8_t ClientList; // username \n username \n username \0
    uint8_t Message; // username :\0 message \0
    uint8_t PrivateMessage; // username :\0 message \0
};
char *list_users_raw();
void send_privitemessage(const char* msg, int length);
void connect_to_server(const char* username, const u_int8_t length);
void send_message(const char* msg, const int length);
void receive(void (*func)(void *, void *, char *, int), void *arg, void *activity);
void closesock();
#endif //CHAT_H