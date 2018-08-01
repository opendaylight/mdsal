############
Architecture
############

.. uml::

   @startuml
   package "MD-SAL Project" {

       () "MD-SAL Binding API" as mdsal.binding.api
       () "MD-SAL DOM API" as mdsal.dom.api
       [Binding Adapter] as mdsal.binding.adapter
       [Binding Data Codec] as mdsal.binding.codec
       [MD-SAL DOM Router] as mdsal.dom.router
       () "MD-SAL Shard SPI" as mdsal.shard.spi

       mdsal.binding.adapter --> mdsal.binding.codec : uses
       mdsal.binding.api -- mdsal.binding.adapter
       mdsal.binding.adapter .> mdsal.dom.api : uses
       mdsal.dom.api -- mdsal.dom.router
       mdsal.dom.router -- mdsal.shard.spi
   }
   @enduml
