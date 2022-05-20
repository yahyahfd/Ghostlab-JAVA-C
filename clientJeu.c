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

void showGames(int descr, char* buff){
    int size_rec = recv(descr,buff,6*sizeof(char),0);//GAMES space
    buff[size_rec] = '\0';
    printf("%s",buff);
    size_rec = recv(descr,buff,sizeof(char),0);//n
    buff[size_rec] ='\0';
    uint8_t n_games = (uint8_t) buff[0];// we store n here
    printf("%d",n_games);
    size_rec = recv(descr,buff,sizeof(char)*3,0);//***
    buff[size_rec]='\0';
    printf("%s\n",buff);
    for(int i=0;i<n_games;i++){
        size_rec = recv(descr,buff,6*sizeof(char),0);
        buff[size_rec] ='\0';
        printf("%s",buff);//OGAME space
        size_rec = recv(descr,buff,sizeof(char),0);
        buff[size_rec] ='\0';
        uint8_t m = (uint8_t) buff[0]; //num game
        printf("%d",m);
        size_rec = recv(descr,buff,2*sizeof(char),0);
        buff[size_rec] ='\0';
        uint8_t s = (uint8_t) buff[1]; //num players
        printf(" %d",s);
        size_rec = recv(descr,buff,3*sizeof(char),0);
        buff[size_rec] ='\0';
        printf("%s\n",buff);//***
    }
}
int main(int argc, char *argv[]) {
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
    if(r!=-1){
        char buff[100];
        showGames(descr,buff);
        int size_rec = 0;
        int rdy = -1;
        int player_created = -1;
        while(rdy == -1){
            int nb = readStdin_bis(buff,26);
            if((nb>=3) && buff[nb-1]== '*' && buff[nb-2]=='*' && buff[nb-3]=='*'){
                char requete[6];
                memcpy(requete, buff, 5);
                requete[6] = '\0';
                if(strcmp(requete,"NEWPL")==0 && nb == 22){
                    char id[9];
                    memcpy(id,&buff[6],8);
                    id[8]='\0';
                    char port[5];
                    memcpy(port,&buff[15],4);
                    port[4]='\0';
                    if(isNumber(port,4)==1 && isAlphaNum(id,8)==1){
                        send(descr,buff,22,0);
                        size_rec = recv(descr,buff,5*sizeof(char),0);
                        buff[size_rec] = '\0';
                        printf("%s",buff);
                        if(strcmp(buff, "REGOK") == 0){
                            size_rec = recv(descr,buff,sizeof(char),0);//space
                            size_rec = recv(descr,buff,sizeof(char),0);//gameid
                            buff[size_rec] ='\0';
                            uint8_t m = (uint8_t) buff[0]; //num game
                            printf(" %d",m);
                            player_created = 1;
                        }
                        size_rec = recv(descr,buff,4*sizeof(char),0);
                        buff[size_rec] ='\0';
                        printf("%s\n",buff);//***
                    }else{
                        printf("L'id est constitué de 8 caractères alphanumériques, et le port de 4 caractères numériques\n");
                    }
                }else if(strcmp(requete,"REGIS")==0 && nb >= 24 && nb<=26){
                    char id[9];
                    memcpy(id,&buff[6],8);
                    id[8]='\0';
                    char port[5];
                    memcpy(port,&buff[15],4);
                    port[4]='\0';
                    int port_valid = -1;
                    if(isNumber(port,4)==1 && strlen(port)==4){
                        port_valid = 0;
                    }
                    char game[nb-22];
                    memcpy(game,&buff[20],nb-23);
                    game[nb-23]='\0';
                    int game_n;
                    int game_valid = -1;
                    if(isNumber(game,nb-23)==1){
                        game_n = atoi(game);
                        if(game_n>=0 && game_n<=255){
                            game_valid = 0;
                        }
                    }
                    if(port_valid == 0 && game_valid == 0){
                        unsigned char *result= malloc(20); //id:8 port:4 REGIS:5 spaces&*:6 m:1
                        memcpy(result,requete,5);
                        memcpy(result+5," ",1);
                        memcpy(result+6,id,8);
                        memcpy(result+14," ",1);
                        memcpy(result+15, port,4);
                        memcpy(result+19," ",1);
                        result[20] ='\0';
                        //we send this first
                        send(descr,result,20,0);
                        uint8_t game_uint = game_n;
                        result[0] = game_uint;
                        result[1] = '\0';
                        send(descr,result,1,0);
                        memcpy(result, "***",3);
                        result[3] ='\0';
                        send(descr,result,3,0);
                        size_rec = recv(descr,buff,5*sizeof(char),0);
                        buff[size_rec] = '\0';
                        printf("%s",buff);
                        if(strcmp(buff, "REGOK") == 0){
                            size_rec = recv(descr,buff,sizeof(char),0);//space
                            size_rec = recv(descr,buff,sizeof(char),0);//gameid
                            buff[size_rec] ='\0';
                            uint8_t m = (uint8_t) buff[0]; //num game
                            printf(" %d",m);
                            player_created = 1;
                        }
                        size_rec = recv(descr,buff,4*sizeof(char),0);
                        buff[size_rec] ='\0';
                        printf("%s\n",buff);//***
                    }else{
                        printf("Port ou numéro de game invalide\n");
                    }
                }else if(strcmp(requete,"START")==0 && nb == 8){
                    if(player_created == 1){
                        send(descr,buff,nb,0);
                        rdy = 1; //on sort du while à partir d'ici
                    }else{
                        printf("Vous devez rejoindre ou créer une partie avant tout\n");
                    }
                }else if(strcmp(requete,"UNREG")==0 && nb == 8){
                    send(descr,buff,nb,0);
                    size_rec = recv(descr,buff,5*sizeof(char),0);
                    buff[size_rec] = '\0';
                    printf("%s",buff);
                    if(strcmp(buff, "UNROK") == 0){
                        size_rec = recv(descr,buff,sizeof(char),0);//space
                        size_rec = recv(descr,buff,sizeof(char),0);//gameid
                        buff[size_rec] ='\0';
                        uint8_t m = (uint8_t) buff[0]; //num game
                        printf(" %d",m);
                        player_created = -1;
                    }
                    size_rec = recv(descr,buff,4*sizeof(char),0);
                    buff[size_rec] ='\0';
                    printf("%s\n",buff);//***
                }else if(strcmp(requete,"SIZE?")==0 && (nb>=10 && nb<= 12)){
                    char game[nb-8];
                    memcpy(game,&buff[6],nb-9);
                    game[nb-9]='\0';
                    int game_n;
                    int game_valid = -1;
                    if(isNumber(game,nb-9)==1){
                        game_n = atoi(game);
                        if(game_n>=0 && game_n<=255){
                            game_valid = 0;
                        }
                    }
                    if(game_valid == 0){
                        unsigned char *result= malloc(6); //req + space
                        memcpy(result,requete,5);
                        memcpy(result+5," ",1);
                        result[6] ='\0';
                        //we send this first
                        send(descr,result,6,0);

                        //game number
                        uint8_t game_uint = game_n;
                        result[0] = game_uint;
                        result[1] = '\0';
                        send(descr,result,1,0);
                        //***
                        memcpy(result, "***",3);
                        result[3] ='\0';
                        send(descr,result,3,0);
                    }
                    size_rec = recv(descr,buff,5*sizeof(char),0);
                    buff[size_rec] = '\0';
                    printf("%s",buff);//SIZE! OR DUNNO
                    if(strcmp(buff,"SIZE!") == 0){
                        size_rec = recv(descr,buff,6*sizeof(char),0);//space m space h space w
                        buff[size_rec] = '\0';
                        uint8_t m = (uint8_t) buff[1];
                        uint8_t h = (uint8_t) buff[3];
                        uint8_t w = (uint8_t) buff[5];
                        printf(" %d %d %d",m,h,w);
                        size_rec = recv(descr,buff,3*sizeof(char),0);//***
                        buff[size_rec] = '\0';
                        printf("%s\n",buff);
                    }else{
                        size_rec = recv(descr,buff,3*sizeof(char),0);//***
                        buff[size_rec] = '\0';
                        printf("%s\n",buff);
                    }
                }else if(strcmp(requete,"LIST?")==0 && (nb>=10 && nb<= 12)){//LIST? m***
                    char game[nb-8];
                    memcpy(game,&buff[6],nb-9);
                    game[nb-9]='\0';
                    int game_n;
                    int game_valid = -1;
                    if(isNumber(game,nb-9)==1){
                        game_n = atoi(game);
                        if(game_n>=0 && game_n<=255){
                            game_valid = 0;
                        }
                    }
                    if(game_valid == 0){
                        unsigned char *result= malloc(6); //req + space
                        memcpy(result,requete,5);
                        memcpy(result+5," ",1);
                        result[6] ='\0';
                        //we send this first
                        send(descr,result,6,0);

                        //game number
                        uint8_t game_uint = game_n;
                        result[0] = game_uint;
                        result[1] = '\0';
                        send(descr,result,1,0);
                        //***
                        memcpy(result, "***",3);
                        result[3] ='\0';
                        send(descr,result,3,0);
                    }
                    size_rec = recv(descr,buff,5*sizeof(char),0);
                    buff[size_rec] = '\0';
                    printf("%s",buff);//LIST! OR DUNNO
                    if(strcmp(buff,"LIST!") == 0){
                        size_rec = recv(descr,buff,4*sizeof(char),0);//space m space s
                        buff[size_rec] = '\0';
                        uint8_t m = (uint8_t) buff[1];
                        uint8_t s = (uint8_t) buff[3];
                        printf(" %d %d",m,s);
                        size_rec = recv(descr,buff,3*sizeof(char),0);//***
                        buff[size_rec] = '\0';
                        printf("%s\n",buff);
                        
                        for(int i = 0;i<s;i++){//s* PLAYR id***
                            size_rec = recv(descr,buff,17*sizeof(char),0);
                            buff[size_rec] = '\0';
                            printf("%s\n",buff);
                        }
                    }else{
                        size_rec = recv(descr,buff,3*sizeof(char),0);//***
                        buff[size_rec] = '\0';
                        printf("%s\n",buff);
                    }
                }else if(strcmp(requete,"GAME?")==0 && nb == 8){
                    send(descr,buff,nb,0);
                    showGames(descr,buff);                    
                }else{
                    printf("Requête non reconnue: le format doit-être respecté entièrement\n");
                }

            }else{
                printf("Message de format REQ <options séparées par \" \">*** et de taille max 24 octets (selon la documentation) attendu\n");
            }
        }

        

        while(player_created != -1){
            //debut game ici et mettre player_created à -1 en fin de game(la socket est fermée par conséquent en sortant de ce while)
        }
        
        close(descr);
    }
    return EXIT_SUCCESS;
}