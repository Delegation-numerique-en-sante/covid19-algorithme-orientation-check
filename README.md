
# Schéma et validation des données issues des questionnaires d'orientation Covid-19

Si votre solution d'orientation COVID-19 est référencée par le ministère des Solidarités et de la Santé, elle implémente [l'algorithme d'orientation Covid-19](https://delegation-numerique-en-sante.github.io/covid19-algorithme-orientation/).

Ce dépôt expose le **schéma de données** de la dernière version de cette documentation et propose en un **outil de validation** des fichiers `csv` envoyés par les producteurs de données.


# Schéma de données

Les fichiers `csv` produits par votre solution doivent respecter [les instructions d'implémentation](https://github.com/Delegation-numerique-en-sante/covid19-algorithme-orientation/blob/master/implementation.org#variables-%C3%A0-obligatoirement-sauvegarder-pour-partage) et le [schéma de données](schema.json) (au format [TableSchema](https://frictionlessdata.io/table-schema/)) de ce dépôt.

Pour produire le fichier `schema.json`:

    ~$ clojure -m schema 2020-04-17


# Outil de validation

L'outil de validation permet de tester un `csv` pour vérifier que les données correspondent au schéma de données et que le message d'orientation du csv correspond au message d'orientation calculé par l'algorithme de référence.

Vous pouvez télécharger le fichier binaire de la [dernière version](https://github.com/Delegation-numerique-en-sante/covid19-algorithme-orientation-check/releases/) puis le lancer ainsi:

    ~$ covid19-algo-check fichier.csv

Deux fichiers seront écrits:

-   `2020-04-18-covid19-orientation-check.csv` : contenant la liste des erreurs concernant l'orientation.
-   `2020-04-18-covid19-errors.txt` : contenant les autres erreurs.


# Compatibilité

Le fichier binaire est compilé pour les architectures **Linux 64 bits**.


# Licence

2020 DINUM, Bastien Guerry.

Le code source de ce dépôt est publié sous [EPL 2.0 license](LICENSE).
