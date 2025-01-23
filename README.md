# BankAccount

Ce projet est une application bancaire utilisant l'architecture hexagonale avec Spring Boot. Il permet de gérer les opérations bancaires courantes telles que les dépôts, les retraits, la consultation du solde et l'historique des transactions. Le projet est organisé en modules Maven pour séparer les différentes responsabilités : api, business, et infra.

# Fonctionnalités

Dépôt d'argent : Ajouter de l'argent à un compte bancaire.

Retrait d'argent : Retirer de l'argent d'un compte bancaire, en assurant que le solde reste positif.

Consultation du solde : Vérifier le solde actuel d'un compte bancaire.

Historique des transactions : Visualiser la liste des transactions effectuées sur le compte (dépôts, retraits).

Validation des montants : Les montants doivent être supérieur à zéro et avoir au maximum deux décimales.

# Structure des Modules

business : Contient la logique métier et les modèles de domaine.

api : Implémente les contrôleurs REST.

infra : Gère la persistance.

# Prérequis

Java : Version 17 ou supérieure

Maven : Version 3.8.1 ou supérieure

Spring Boot : Version 3.0.0 ou supérieure
