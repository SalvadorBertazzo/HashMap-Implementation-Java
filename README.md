# HashMap-Implementation-Java
Trabajo Practico de la materia Tecnologia del Software Base - Ingenieria en Sistemas de Infomacion - UTN 2022

## Consignas
- Se solicita hacer una implementación rigurosa de la estructura de tabla hash con resolución de colisiones por direccionamiento abierto.
- La implementacion debe contener:
  + Implementar la interface Map<K, V> y desde ella, los mismos métodos que sean necesarios. Para aliviar la tarea, los estudiantes pueden derivar la clase AbstractMap, que ya implementa en forma concreta varios de los métodos de la interface Map, pero dejando claro lo siguiente: los estudiantes obligatoriamente deberán dar sus propias implementaciones de los métodos que se testean en la Junit provista por la Cátedra en forma anexa a este enunciado.
  + La clase debe controlar que ninguno de los dos valores key y value de cada par almacenado sea null (emulando así lo que hace la clase Hashtable nativa de Java). Y por otra parte, la clase implementada no debe tener en cuenta el control thread-safe (en forma similar a lo que hace HashMap).
  + Definir dentro de la clase TSBHashTableDA una clase interna Entry que implemente la interface Map.Entry<K, V> para representar a cada par que se almacene en la tabla.
  + Definir dentro de la clase TSBHashTableDA las tres clases internas para gestionar las vistas stateless de claves, de valores y de pares de la tabla, incluyendo a su vez en ellas las clases internas para representar a los iteradores asociados a cada vista.
  + Redefinir en la clase TSBHashTableDA los métodos equals(), hashCode(), clone() y toString() que se heredan desde Object. Para equals() y hashCode().
  + Definir en la clase TSBHashTableDA los métodos rehash() y contains(value) que no vienen especificados por Map, pero son especialmente propios de la clase (emulando a java.util.Hashtable).
