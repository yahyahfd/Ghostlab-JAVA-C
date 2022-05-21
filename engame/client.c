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

  struct sockaddr_in adress_sock;
  adress_sock.sin_family = AF_INET;
  int port_rd = atoi(argv[1]);
  if(port_rd==0){
      exit(EXIT_FAILURE);
  }
  adress_sock.sin_port = htons(port_rd); //PORT
  int ip_rd = inet_aton(argv[2],&adress_sock.sin_addr); //IP
  if(ip_rd==0){
      exit(EXIT_FAILURE);
  }
  int descr=socket(PF_INET,SOCK_STREAM,0);
  if(descr == -1){
      exit(EXIT_FAILURE);
  }
  int r=connect(descr,(struct sockaddr *)&adress_sock,sizeof(struct sockaddr_in));
  if(r!=-1){
    while(1) {
      int size_rec = 0;
      char buff[100];
      int nb = readStdin_bis(buff,12);
      printf("buffer entier juste apres stdin:%s\n",buff);
      if((nb>=3) && buff[nb-1]== '*' && buff[nb-2]=='*' && buff[nb-3]=='*'){
        char requete[6];
        memcpy(requete,buff, 5);
        requete[5] = '\0';
        printf("req:%s\n",requete);
         if((strcmp(requete,"UPMOV")==0 || strcmp(requete,"DOMOV") || strcmp(requete,"LEMOV") || strcmp(requete,"RIMOV")) && nb == 12){
           printf("buffer avant envoie:%s\n",buff);
           send(descr,buff,12,0);
           printf("sent req\n");
           size_rec = recv(descr,buff,5*sizeof(char),0);
           buff[size_rec] = '\0';
           printf("%s",buff);
           char touched[6];
           memcpy(touched, buff, 5);
           touched[5] = '\0';
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

           if(strcmp(touched,"MOVEF")==0){
             size_rec = recv(descr,buff,sizeof(char),0);//space
             size_rec = recv(descr,buff,4*sizeof(char),0);
             buff[size_rec] ='\0';
             for (size_t i = 0; i < 4; i++) { //4 octets du score
               printf(" %d",(uint8_t) buff[i]);
             }

           }
           size_rec = recv(descr,buff,3*sizeof(char),0);
           buff[size_rec] ='\0';
           printf("%s\n",buff);//***


        }else if(strcmp(requete,"IQUIT")==0 && nb == 8){
          send(descr,buff,8,0);
          size_rec = recv(descr,buff,8*sizeof(char),0);
          buff[size_rec] = '\0';
          printf("%s",buff);
          close(descr);
          return EXIT_SUCCESS;
        }else if(strcmp(requete,"GLIS?")==0 && nb == 8){
          send(descr,buff,8,0);
          size_rec = recv(descr,buff,5*sizeof(char),0);//requete
          buff[size_rec] = '\0';
          printf("%s",buff);

          size_rec = recv(descr,buff,sizeof(char),0);//space

          size_rec = recv(descr,buff,sizeof(char),0); //nb de joueur
          buff[size_rec] ='\0';
          uint8_t m = (uint8_t) buff[0];
          printf(" %d",m);

          size_rec = recv(descr,buff,3*sizeof(char),0);
          buff[size_rec] ='\0';
          printf("%s\n",buff);//***

          for (size_t i = 0; i < (int)m; i++) { //[GPLYR␣id␣x␣y␣p***] s fois
            size_rec = recv(descr,buff,5*sizeof(char),0);//requete
            buff[size_rec] = '\0';
            printf("%s",buff);


            size_rec = recv(descr,buff,sizeof(char),0);//space

            size_rec = recv(descr,buff,8*sizeof(char),0);//id
            buff[size_rec] = '\0';
            printf("%s",buff);

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

            size_rec = recv(descr,buff,4*sizeof(char),0);
            buff[size_rec] ='\0';
            for (size_t i = 0; i < 4; i++) { //4 octets du score
              printf(" %d",(uint8_t) buff[i]);
            }

            size_rec = recv(descr,buff,3*sizeof(char),0);
            buff[size_rec] ='\0';
            printf("%s\n",buff);//***
          }

        }else if(strcmp(requete,"MALL?")==0 && nb == 8){

        }else if(strcmp(requete,"SEND?")==0 && nb == 8){

        }else{
          printf("bad command\n");
        }
       }
    }

  }
  close(descr);
  return 0;
}
