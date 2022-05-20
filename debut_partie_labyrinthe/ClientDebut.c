#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <ctype.h>


int readStdin(int toRead,char* buf){
    int nb = 0;
    int ch;
    while ((ch = fgetc(stdin)) != EOF) {  // Read until EOF ...
        if (nb + 1 <= toRead) {
            buf[nb++] = ch;
        }
        if (ch == '\n') {  // ... or end of line
            break;  
        }
    } 
    buf[nb] = '\0';
    return nb;
}

int readStdin_bis(char *buf, int size){
    int size_bis = 0;
    int ch;
    int count = 0;
    while((ch = fgetc(stdin)) != EOF && (ch !='\n') && (count!=3)){
        if(ch == '*'){
            count++;
            // printf("here is %d:%c\n",count,ch);
        }
        if (size_bis + 1 <= size) {
            buf[size_bis++] = ch;
        }
    }
    buf[size_bis] = '\0';
    return size_bis;
}

int isNumber(char * buf,int nb){
    int digits = 0;
    for(int i = 0;i<nb;i++){
        if(isdigit(buf[i])!=0){
            digits++;
        }
    }
    // printf("readdigts:%d\n",digits);
    // printf("readnb:%d\n",nb);
    if(digits == nb){
        return 1;
    }
    return 0;
}

int isAlphaNum(char * buf, int nb){
    int a = 0;
    for(int i = 0;i<nb;i++){
        if(isalnum(buf[i])!=0){
            a++;
        }
    }
    if(a == nb){
        return 1;
    }
    return 0;
}


int main(int argc, char *argv[]){
    if(argc < 3){
        printf("Port et IP attendus ici\n");
        exit(EXIT_FAILURE);
    }
    struct sockaddr_in adress_sock;
    adress_sock.sin_family = AF_INET;
    adress_sock.sin_port = htons(atoi(argv[1])); //PORT
    inet_aton(argv[2],&adress_sock.sin_addr); //IP
    int descr=socket(PF_INET,SOCK_STREAM,0);
    int r=connect(descr,(struct sockaddr *)&adress_sock,sizeof(struct sockaddr_in));
    if(r!=1){
        char buff[100];
        int size_rec = 0;


        //Apres while(rdy== -1) en assumant que rdy = 1
        size_rec = recv(descr,buff,5*sizeof(char),0); //Welco
        buff[size_rec] = '\0';
        printf("%s", buff);
        if(strcmp(buff,"WELCO") == 0){
            size_rec = recv(descr,buff,9*sizeof(char),0); //space m space h space w space f space
            buff[size_rec] = '\0';
            uint8_t m = (uint8_t) buff[1];
            uint8_t h = (uint8_t) buff[3];
            uint8_t w = (uint8_t) buff[5];
            uint8_t f = (uint8_t) buff[7];
            printf(" %d %d %d %d",m,h,w,f);
            size_rec = recv(descr,buff,20*sizeof(char),0); //IP space port * * *
            buff[size_rec] = '\0';
            printf("%s\n",buff);
        
            size_rec = recv(descr,buff,5*sizeof(char),0);
            buff[size_rec] = '\0';
            printf("%s",buff); //POSIT
            if(strcmp(buff,"POSIT") == 0){
                size_rec = recv(descr,buff,20*sizeof(char),0);
                buff[size_rec] = '\0';
                printf("%s\n", buff);
            }
        }


    }else{
        printf("Requete non reconnu")
    }

}