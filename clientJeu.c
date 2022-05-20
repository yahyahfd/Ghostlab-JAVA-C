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
        printf(" %d ",s);
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
                printf("req:%s\n",requete);
                if(strcmp(requete,"NEWPL")==0 && nb == 22){
                    char id[9];
                    memcpy(id,&buff[6],8);
                    id[8]='\0';
                    char port[5];
                    memcpy(port,&buff[15],4);
                    port[4]='\0';
                    if(isNumber(port,4)==1 && isAlphaNum(id,8)==1){
                        send(descr,buff,22,0);
                        printf("sent req\n");//
                        size_rec = recv(descr,buff,5*sizeof(char),0);
                        buff[size_rec] = '\0';
                        printf("%s",buff);
                        // printf("%d",(uint8_t)buff[6]);
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
                    printf("game:%s\n",game);//
                    int game_valid = -1;
                    if(isNumber(game,nb-23)==1){
                        game_n = atoi(game);
                        printf("game_n:%d\n",game_n);//
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
                        // printf("%d",(uint8_t)buff[6]);
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
                    // printf("%d",(uint8_t)buff[6]);
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
                }else if(strcmp(requete,"SIZE?")==0 && nb == 10){
                    
                }else if(strcmp(requete,"LIST?")==0 && (nb>=10 && nb<= 12)){//LIST? m***
                    char game[nb-8];
                    memcpy(game,&buff[6],nb-9);
                    game[nb-9]='\0';
                    int game_n;
                    int game_valid = -1;
                    if(isNumber(game,nb-9)==1){
                        game_n = atoi(game);
                        printf("game_n:%d\n",game_n);//
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


        // int id_chosen = -1;
        // char * id;
        // int nb;
        // while(id_chosen == -1){
        //     printf("Choisissez un identifiant de taille exactement 8 caractères alphanumériques (tronqué à 8):\n");
        //     nb = readStdin(8,buff);
        //     if(nb == 8 && buff[7]!= '\n'){
        //         id_chosen = 1;
        //         id = malloc(nb);
        //         memcpy(id, buff, nb);
        //     }else{
        //         printf("Une chaine de 8 caractères alphanumériques était attendue ici\n");
        //     }
        // }

        // int udp_chosen = -1;
        // char * port;
        // while(udp_chosen == -1){
        //     printf("Choisissez un port UPD pour recevoir des messages(tronqué à 4):\n");
        //     nb = readStdin(4,buff);
        //     if(isNumber(buff,nb)==1 && nb == 4){//Nombre de 4 chars
        //         udp_chosen = 1;
        //         port = malloc(nb);
        //         memcpy(port,buff,nb);
        //     }else{
        //         printf("Un port contenant 4 caractères numériques attendu ici\n");
        //     }
        // }

        
        // int game_chosen = -1; //while (-1) we ask again and again
        // while(game_chosen == -1){
        //     printf("Type +++ if you want to create a new game, or the number corresponding to an existing game (0-255) you want to join:\n");
        //     nb = readStdin(3,buff);
        //     if(isNumber(buff,nb)==1){
        //         int m = atoi(buff);
        //         char * game_n = malloc(nb);
        //         memcpy(game_n,buff,nb);
        //         printf("m:%d\n",m);
        //         if(m>=0 && m<=255){
        //             game_chosen = 1;
        //             char *result= malloc(24); //id:8 port:4 NEWPL:5 spaces&*:6 m:1
        //             strcpy(result, "REGIS ");
        //             strcat(result, id);
        //             strcat(result, " ");
        //             strcat(result, port);
        //             strcat(result, " ");
        //             // uint8_t mb = m;
        //             // buff[0] = mb;
        //             // buff[1] = '\0';
        //             strcat(result, game_n);
        //             strcat(result, "***");
        //             send(descr,result,strlen(result),0);//REGIS id port m***
        //         }else{
        //             printf("A number between 0 and 255 was expected here\n");
        //         }
        //     }else{
        //         if(strcmp(buff,"+++")==0){//On crée une nouvelle partie
        //             game_chosen = 2;
        //             char *result= malloc(22); //id:8 port:4 NEWPL:5 spaces&*:5
        //             strcpy(result, "NEWPL ");
        //             strcat(result, id);
        //             strcat(result, " ");
        //             strcat(result, port);
        //             strcat(result, "***");
        //             send(descr,result,strlen(result),0);//NEWPL id port***
        //         }else{
        //             printf(" \"+++\" was expected here to create a new game\n");
        //         }
        //     }
        // }
        

        // printf("%d\n",ze);
       
        // if(isNum(buff)==1){
        //     int m = atoi(buff);
            // if(m>=0 && m<=255){
            //     uint8_t mb = m;
            //     buff[0] = mb;
            //     buff[1] = '\0';
            //     send(descr,buff,1,0);
            // }else{
            //     printf("A number between 0 and 255 was expected here\n");
            //     buff[0] = 0;
            //     buff[1] = '\0';
            //     send(descr,buff,1,0);
        //     }
        // }
// char buf[SIZE];
// fgets(buf, SIZE, stdin);

// or
// scanf("%255s", buf);
// uint32_t byteTmp;
// scanf("%2x", &byteTmp);
// uint8_t byte = byteTmp;
// uint8_t m;
// scanf(%)
//         scanf("%")
        // uint8_t d;
        // scanf("%c", buff);
        // d = buff[0];
        // printf("%d\n", d); 

        // printf("%d",nb);
        // send(descr,buff,nb,0);
        // uint8_t m = (uint8_t) buff;
        // printf("%d\n",m);
        
       

    


        








    // int nb;
        
    //     printf("Choose an UDP port for receiving messages:\n");
    //     nb = read(STDIN_FILENO, buff, 4);
    //     if(nb == -1){
    //         printf("There was a problem choosing a port\n");
    //         exit(EXIT_FAILURE);
    //     }
    //     buff[nb] = '\0';
    //     send(descr,buff,nb,0);
            //send these two infos right now
            
//             char *mess="NEWPL ";
//             char buff2 []
// const char* name = "hello";
// const char* extension = ".txt";

// char* name_with_extension;
// name_with_extension = malloc(strlen(name)+1+4); /* make space for the new string (should check the return value ...) */
// strcpy(name_with_extension, name); /* copy name into the new var */
// strcat(name_with_extension, extension); /* add the extension */
// ...
// free(name_with_extension);




            // send(descr,mess,strlen(mess),0);
        

        // printf("Caracteres recus : %d\n",total);



        close(descr);
    }
    return EXIT_SUCCESS;
}