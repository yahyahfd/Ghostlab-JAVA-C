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
        int total = 0;
        int size_rec = recv(descr,buff,6*sizeof(char),0);
        total += size_rec;
        buff[size_rec] = '\0';
        printf("Message : %s",buff);
        size_rec = recv(descr,buff,sizeof(char),0);
        total += size_rec;
        buff[size_rec] ='\0';
        uint8_t x = (uint8_t) buff[0];
        printf("%d",x);
        size_rec = recv(descr,buff,sizeof(char)*3,0);
        total += size_rec;
        buff[size_rec]='\0';
        printf("%s\n",buff);
        for(int i=1;i<=x;i++){
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
        int id_chosen = -1;
        char * id;
        int nb;
        while(id_chosen == -1){
            printf("Choisissez un identifiant de taille exactement 8 caractères alphanumériques (tronqué à 8):\n");
            nb = readStdin(8,buff);
            if(nb == 8 && buff[7]!= '\n'){
                id_chosen = 1;
                id = malloc(nb);
                memcpy(id, buff, nb);
            }else{
                printf("Une chaine de 8 caractères alphanumériques était attendue ici\n");
            }
        }

        int udp_chosen = -1;
        char * port;
        while(udp_chosen == -1){
            printf("Choisissez un port UPD pour recevoir des messages(tronqué à 4):\n");
            nb = readStdin(4,buff);
            if(isNumber(buff,nb)==1 && nb == 4){//Nombre de 4 chars
                udp_chosen = 1;
                port = malloc(nb);
                memcpy(port,buff,nb);
            }else{
                printf("Un port contenant 4 caractères numériques attendu ici\n");
            }
        }

        
        int game_chosen = -1; //while (-1) we ask again and again
        while(game_chosen == -1){
            printf("Type +++ if you want to create a new game, or the number corresponding to an existing game (0-255) you want to join:\n");
            nb = readStdin(3,buff);
            if(isNumber(buff,nb)==1){
                int m = atoi(buff);
                char * game_n = malloc(nb);
                memcpy(game_n,buff,nb);
                printf("m:%d\n",m);
                if(m>=0 && m<=255){
                    game_chosen = 1;
                    char *result= malloc(24); //id:8 port:4 NEWPL:5 spaces&*:6 m:1
                    strcpy(result, "REGIS ");
                    strcat(result, id);
                    strcat(result, " ");
                    strcat(result, port);
                    strcat(result, " ");
                    // uint8_t mb = m;
                    // buff[0] = mb;
                    // buff[1] = '\0';
                    strcat(result, game_n);
                    strcat(result, "***");
                    send(descr,result,strlen(result),0);//REGIS id port m***
                }else{
                    printf("A number between 0 and 255 was expected here\n");
                }
            }else{
                if(strcmp(buff,"+++")==0){//On crée une nouvelle partie
                    game_chosen = 2;
                    char *result= malloc(22); //id:8 port:4 NEWPL:5 spaces&*:5
                    strcpy(result, "NEWPL ");
                    strcat(result, id);
                    strcat(result, " ");
                    strcat(result, port);
                    strcat(result, "***");
                    send(descr,result,strlen(result),0);//NEWPL id port***
                }else{
                    printf(" \"+++\" was expected here to create a new game\n");
                }
            }
        }
        

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