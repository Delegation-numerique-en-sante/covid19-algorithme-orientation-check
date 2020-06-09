## Schéma et validation des données issues des questionnaires d'orientation Covid-19

Si votre solution d'orientation Covid-19 est référencée par le ministère des Solidarités et de la Santé, elle suite la [documentation de l'algorithme d'orientation Covid-19](https://delegation-numerique-en-sante.github.io/covid19-algorithme-orientation/).

Ce dépôt expose le **schéma de données** de cette documentation et propose un **outil de validation** des fichiers `csv` envoyés par les producteurs de données.

Résumé des commandes:

	# Générer le fichier .jar
	~$ clj -A:jar

	# Générer le schéma:
	~$ java -cp covid19-check.jar clojure.main -m core make-schema

	# Générer un fichier csv d'exemple:
	~$ java -cp covid19-check.jar clojure.main -m core make-csv

	# Tester le schéma d'un fichier csv:
	~$ java -cp covid19-check.jar clojure.main -m core check-schema example.csv

	# Tester la validité des messages d'orientation du fichier csv:
	~$ java -cp covid19-check.jar clojure.main -m core check-algo example.csv

## Schéma de données

Les fichiers `csv` produits par votre solution doivent respecter [les instructions d'implémentation](https://github.com/Delegation-numerique-en-sante/covid19-algorithme-orientation/blob/master/implementation.org#variables-%C3%A0-obligatoirement-sauvegarder-pour-partage) et le [schéma de données](schema.json) (au format [TableSchema](https://frictionlessdata.io/table-schema/)) de ce dépôt.

## Exemples

Ce dépôt contient un exemple de `csv` pour la dernière version du schéma, `example.csv`.

## Vous souhaitez publier des données anonymisées ?

Nous vous invitons à respecter le schéma de données, prendre contact avec `mobilisation-covid@sante.gouv.fr` pour leur indiquer que vous souhaitez partager vos données avec le ministère des Solidarités et de la Santé et à publier vos données anonymisées sur [data.gouv.fr](https://www.data.gouv.fr/fr/).

## Validation de la conformité d'un fichier `csv` à ce schéma

Le schéma publié dans ce dépôt suit les spécifications TableSchema.

Vous pouvez utiliser un outil comme [goodtables](https://github.com/frictionlessdata/goodtables-py) pour vérifier que vos fichiers `csv` sont conformes à ce schéma.

## Validation de la conformité d'un fichier `csv` à l'algorithme d'orientation

L'outil de validation disponible depuis ce dépôt (`covid19-check.jar`) permet de vérifier que le message d'orientation contenu dans le champ `orientation` de votre `csv` correspond au message d'orientation calculé par l'algorithme d'orientation de [référence](https://delegation-numerique-en-sante.github.io/covid19-algorithme-orientation/).

Vous pouvez télécharger le fichier `jar` de la [dernière version](https://github.com/Delegation-numerique-en-sante/covid19-algorithme-orientation-check/releases/).

## Licence

2020 DINUM, Bastien Guerry.

Le code source de ce dépôt est publié sous [EPL 2.0 license](LICENSE).
