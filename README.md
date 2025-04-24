# Projet Java ING1 - Débruitage d'image par ACP

Ce projet implémente une application de débruitage d'images utilisant l'Analyse en Composantes Principales (ACP).
L'application permet à l'utilisateur de charger une image, d'y appliquer du bruit ou alors de la débruiter.

## Caractéristiques

- Interface graphique développée avec JavaFX
- Ajout de bruit d'intensité variable
- Algorithme de débruitage basé sur l'ACP

## Installation

### Prérequis

- Java JDK 11 ou supérieur
- JavaFX 11 ou supérieur

### Installation en local

Pour lancer ce projet, il suffit de cloner le répertoire Git dans un système UNIX.

**Étape 1 : Cloner le répertoire**
```bash
git clone https://github.com/P33TheWhite/Projet-Java-ING1.git
```

**Étape 2 : Naviguer vers le dossier source**
```bash
cd Projet-Java-ING1/ProjetDenoising/src
```

**Étape 3 : Compiler et exécuter l'application**
```bash
javac projetdenoising/ui/MainApp.java
java projetdenoising.ui.MainApp
```

## Utilisation

1. Lancez l'application
2. Sélectionnez si vous voulez un débruitage ou un bruitage
3. Cliquez sur "Choisir une image" pour sélectionner une image à traiter
4.
5.
6. Utilisez "Enregistrer l'image" pour sauvegarder le résultat

## Structure du projet

- `projetdenoising.ui` : Classes liées à l'interface utilisateur
- `projetdenoising.model` : Modèles de données
- `projetdenoising.service` : Services de traitement d'image & Algorithmes mathématiques 
- `projetdenoising.math` : Methode statique reutilisable

## Fonctionnement technique

L'application est implémenté sur une architecture MVC (Modèle-Vue-Contrôleur).

Le processus de débruitage comprend plusieurs étapes :

1. 
2. 

## Contribution

- LE BLANC Paul
- TEMSAMANI Yassir
- MARCO Oihana
- METZ Antoine
- BOUMEDINE Imrane
