obj = {
    name: 'language', // наименование актора
    package: 'annette.core.services', // корневой пакет, опционально
    modelPackage: 'annette.core.domain.language.model',  // пакет с моделью данных
    //actorPackage: 'annette.uic.staff',  // пакет с моделью данных
    actorName: 'LanguageActor',
    stateName: 'LanguageState',
    objectName: 'LanguageService',

    // imports: [ 'java.util.UUID' ],

    entities: [
        {
            name: 'language',
            pluralName: 'languages',
            commands: [
                {create: true},
                {update: true},
                {del: true},
            ],
            queries: [
                {byId: true},
                {all: true}
            ]
        }
    ]
}