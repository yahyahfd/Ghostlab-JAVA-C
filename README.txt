# PR6_HOH

Dans un terminal, premier terminal compiler le serveur via la commande "javac serveurJeu.java", puis lancer le serveur via la commande 
"java serveurJeu <port au choix>".
Dans un ou plusieurs autres terminaux après compilation du client via "gcc -Wall -o client clientJeu.c", lancer "./client <port> <ip>"

Nous avons choisi de faire le serveur en java, parce qu'on est plus à l'aise en java concernant les objets et la gestions d'objets, en l'occurence ici, il s'agit
des classes "Fantome.java", "Labyrinthe.java", "Mur.java", "Partie.java", "Joueur.java" et "Case.java" qui sont toutes utilisées pour gérer une partie 
(avant et pendant partie). Toutes les classes sont dans le même dossier.