#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <ctype.h>

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

int main(int argc, char const *argv[]) {
   int nb = readStdin_bis(buff,219);
   char buff[250];
   if((nb>=3) && buff[nb-1]== '*' && buff[nb-2]=='*' && buff[nb-3]=='*'){
     char requete[6];
     memcpy(requete, buff, 5);
     requete[6] = '\0';
     printf("req:%s\n",requete);
     if(strcmp(requete,"UPMOV")==0 && nb == 12){
       send(descr,buff,22,0);
       printf("sent req\n");
       size_rec = recv(descr,buff,5*sizeof(char),0);
       buff[size_rec] = '\0';
       printf("%s",buff);
       if(strcmp(buff, "MOVEF") == 0){
         size_rec = recv(descr,buff,sizeof(char),0);//space
         size_rec = recv(descr,buff,3*sizeof(char),0);
         buff[size_rec] ='\0';
         for (size_t i = 0; i < 3; i++) { //3 octets de position X
           printf(" %d",(uint8_t) buff[i]);
         }
         size_rec = recv(descr,buff,sizeof(char),0);//space
         size_rec = recv(descr,buff,3*sizeof(char),0);
         buff[size_rec] ='\0';
         for (size_t i = 0; i < 3; i++) { //3 octets de position Y
           printf(" %d",(uint8_t) buff[i]);
         }
         size_rec = recv(descr,buff,sizeof(char),0);//space
         size_rec = recv(descr,buff,4*sizeof(char),0);
         buff[size_rec] ='\0';
         for (size_t i = 0; i < 4; i++) { //4 octets du score
           printf(" %d",(uint8_t) buff[i]);
         }
         size_rec = recv(descr,buff,4*sizeof(char),0);
         buff[size_rec] ='\0';
         printf("%s\n",buff);//***

       }else if(strcmp(buff, "MOVE!") == 0){

       }


    }else if(strcmp(requete,"DOMOV")==0 && nb == 12){

    }else if(strcmp(requete,"LEMOV")==0 && nb == 12){

    }else if(strcmp(requete,"RIMOV")==0 && nb == 12){

    }else if(strcmp(requete,"IQUIT")==0 && nb == 8){

    }else if(strcmp(requete,"GLIS?")==0 && nb == 10){

    }else if(strcmp(requete,"MALL?")==0 && nb == 8){

    }else if(strcmp(requete,"SEND?")==0 && nb == 8){

    }
   }
  return 0;
}
