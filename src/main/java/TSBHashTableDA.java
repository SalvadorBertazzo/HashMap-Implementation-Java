import java.io.Serializable;
import java.util.*;

public class TSBHashTableDA<K, V> extends AbstractMap<K, V> implements Cloneable, Serializable {

    // Atributos
    private final static int MAX_SIZE = Integer.MAX_VALUE; // Maxima cantidad de elementos que puede contener la tabla
    private Entry<K, V> tabla[]; // Tabla
    private int initial_capacity; // Capacidad inicial de la tabla, se usa en caso de querer resetearla
    private int count; // Cantidad de objetos que tiene la tabla
    private float load_factor; // Factor de carga, utilizado para saber cuando es necesario hacer rehashing
    protected transient int modCount; // Conteo de operaciones de cambio de tamano
    private int states[];

    // Constructores (Sobrecarga de constructores)
    public TSBHashTableDA() {
        this(50, 0.75f);
    }

    public TSBHashTableDA(int initial_capacity) {
        this(initial_capacity, 0.75f);
    }

    public TSBHashTableDA(int initial_capacity, float load_factor) {
        // Cheackeamos condiciones antes de crear la tabla
        if (initial_capacity < 0) {
            initial_capacity = 50;
        }
        if (load_factor < 0) {
            load_factor = 0.75f;
        }
        if (MAX_SIZE < initial_capacity) {
            initial_capacity = MAX_SIZE;
        }
        initial_capacity = this.siguientePrimo(initial_capacity);

        // Creamos la tabla
        this.tabla = new Entry[initial_capacity];
        states = new int[initial_capacity];

        // Actualizamos valores de los atributos
        this.initial_capacity = initial_capacity;
        this.load_factor = load_factor;
        this.count = 0;
        this.modCount = 0;
    }

    // Metodos
    public TSBHashTableDA(Map<? extends K, ? extends V> t) {
        this(53, 0.75f);
        this.putAll(t);
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public boolean isEmpty() {
        return (this.count == 0);
    }

    @Override
    public boolean containsKey(Object key) {
        return (this.get((K) key) != null);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.contains(value);
    }

    public V get(Object key) {
        if (key == null)
            throw new NullPointerException("get(): parámetro null");

        int ih = this.h((K) key);
        int ic = ih;
        int j = 1;
        V valueReturn = null;

        while (this.states[ic] != 0) {
            if (this.states[ic] == 1) {
                Entry<K, V> entry = this.tabla[ic];

                if (key.equals(entry.getKey())) {
                    valueReturn = entry.getValue();
                    return valueReturn;
                }
            }

            ic += j * j;
            j++;
            if (ic >= this.tabla.length) {
                ic %= this.tabla.length;
            }
        }

        return valueReturn;
    }

    @Override
    public V put(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException("put(): parámetro null");

        int ih = this.h(key);
        int ic = ih;
        int first_tombstone = -1;
        int j = 1;
        V old = null;

        while (this.states[ic] != 0) {

            if (this.states[ic] == 1) {
                Entry<K, V> entry = this.tabla[ic];
                if (key.equals(entry.getKey())) {
                    old = entry.getValue();
                    entry.setValue(value);
                    this.count++;
                    this.modCount++;

                    return old;
                }
            }

            if (this.states[ic] == 2 && first_tombstone < 0) first_tombstone = ic;

            ic += j * j;
            j++;
            if (ic >= this.tabla.length) {
                ic %= this.tabla.length;
            }
        }

        if (first_tombstone >= 0) ic = first_tombstone;

        this.tabla[ic] = new Entry<K, V>(key, value);
        this.states[ic] = 1;

        this.count++;
        this.modCount++;

        float fc = (float) count / (float) this.tabla.length;
        if (fc >= this.load_factor)
            this.rehash();

        return old;
    }

    @Override
    public V remove(Object key) {
        if (key == null)
            throw new NullPointerException("remove(): parámetro null");

        int ih = this.h((K) key);
        int ic = ih;
        int j = 1;
        V old = null;

        // Busco el elemento a eliminar
        while (this.states[ic] != 0) {

            if (this.states[ic] == 1) {
                Entry<K, V> entry = this.tabla[ic];

                if (key.equals(entry.getKey())) {
                    old = entry.getValue();
                    this.tabla[ic] = null;
                    this.states[ic] = 2;

                    this.count--;
                    this.modCount++;

                    return old;
                }
            }

            ic += j * j;
            j++;
            if (ic >= this.tabla.length) {
                ic %= this.tabla.length;
            }
        }

        return old;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {

        this.tabla = new Entry[this.initial_capacity];

        states = new int[this.initial_capacity];

        for (int i = 0; i < states.length; i++) {
            states[i] = 0;
        }

        this.count = 0;
        this.modCount++;
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new ValueCollection();
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    public boolean equals(Object objeto) {
        if (!(objeto instanceof Map)) {
            return false;
        }
        Map<K, V> tabla = (Map<K, V>) objeto;
        if (this.size() != tabla.size()) {
            return false;
        }

        try {
            Iterator<Map.Entry<K, V>> i = this.entrySet.iterator();
            while (i.hasNext()) {
                Map.Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (tabla.get(key) == null) {
                    return false;
                } else {
                    if (!value.equals(tabla.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException e) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (this.isEmpty()) {
            return 0;
        }

        return this.tabla.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("");
        string.append("-TABLA- \n [");

        for (int i = 0; i < this.tabla.length; i++) {
            if (this.tabla[i] != null) {
                string.append("\t").append(this.tabla[i].toString()).append("\n");
            } else {
                string.append("\t[]\n");
            }
        }
        string.append("]");

        return string.toString();
    }

    @Override
    protected TSBHashTableDA clone() throws CloneNotSupportedException {
        TSBHashTableDA<K, V> tabla = new TSBHashTableDA<>(this.initial_capacity, this.load_factor);

        for (Map.Entry<K, V> entry : this.entrySet()) {
            tabla.put(entry.getKey(), entry.getValue());
        }

        return tabla;
    }

    protected void rehash() {
        int old_length = this.tabla.length;

        int new_length = siguientePrimo(old_length * 2 + 1);

        if (new_length > TSBHashTableDA.MAX_SIZE) {
            new_length = TSBHashTableDA.MAX_SIZE;
        }

        Entry<K, V> tempTable[] = new Entry[new_length];
        int tempStates[] = new int[new_length];

        for (int i = 0; i < tempStates.length; i++) tempStates[i] = 0;

        this.modCount++;

        for (int i = 0; i < this.tabla.length; i++) {
            if (this.states[i] == 1) {

                Entry<K, V> x = this.tabla[i];

                K key = x.getKey();
                int y = this.h(key, tempTable.length);
                int ic = y, j = 1;

                while (tempStates[ic] != 0) {
                    ic += j * j;
                    j++;
                    if (ic >= tempTable.length) {
                        ic %= tempTable.length;
                    }
                }

                tempTable[ic] = x;
                tempStates[ic] = 1;
            }
        }

        this.tabla = tempTable;
        this.states = tempStates;
    }

    public boolean contains(Object value) {
        if (value == null)
            return false;

        Iterator<Map.Entry<K, V>> it = this.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            if (value.equals(entry.getValue()))
                return true;
        }

        return false;
    }

    private int h(int k) {
        return h(k, this.tabla.length);
    }

    private int h(K key) {
        return h(key.hashCode(), this.tabla.length);
    }

    private int h(K key, int t) {
        return h(key.hashCode(), t);
    }

    private int h(int k, int t) {
        if (k < 0)
            k *= -1;
        return k % t;
    }

    private int siguientePrimo(int numero) {
        if (numero % 2 == 0) numero++;
        for (; !esPrimo(numero); numero += 2) ;
        return numero;
    }

    private boolean esPrimo(int numero) {
        for (int i = 3; i < (int) Math.sqrt(numero); i += 2) {
            if (numero % i == 0) {
                return false;
            }
        }
        return true;
    }

    // Clase privada Entry
    private class Entry<K, V> implements Map.Entry<K, V> {

        // Atributos
        private K key;
        private V value;

        // Contructor
        public Entry(K key, V value) {
            if (key == null || value == null) {
                throw new Error("Parametro null");
            }
            this.key = key;
            this.value = value;
        }

        // Getters y Setters
        @Override
        public K getKey() {
            return key;
        }

        public K setKey(K key) {
            K old = this.key;
            if (key == null) {
                throw new Error("Parametro null");
            }
            this.key = key;
            return old;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            if (value == null) {
                throw new Error("Parametro null");
            }
            this.value = value;
            return old;
        }

        // Metodos
        @Override
        public int hashCode() {
            int hash = 7;

            hash = 61 * hash + Objects.hashCode(this.key);
            hash = 61 * hash + Objects.hashCode(this.value);

            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != this.getClass()) {
                return false;
            }

            Entry compareObject = (Entry) obj;
            if (!Objects.equals(this.key, compareObject.key)) {
                return false;
            }
            if (!Objects.equals(this.value, compareObject.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "[" + key.toString() + " - " + value.toString() + " ]";
        }
    }

    // Definimos Vistas
    private transient Set<K> keySet = null;
    private transient Set<Map.Entry<K, V>> entrySet = null;
    private transient Collection<V> values = null;

    // Clase privada KeySet
    private class KeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        @Override
        public int size() {
            return TSBHashTableDA.this.count;
        }

        @Override
        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return (TSBHashTableDA.this.remove(o) != null);
        }

        @Override
        public void clear() {
            TSBHashTableDA.this.clear();
        }

        private class KeySetIterator implements Iterator<K> {

            private int last_entry;
            private int current_entry;
            private boolean next_ok;
            private int expected_modCount;

            public KeySetIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            @Override
            public boolean hasNext() {
                Entry<K, V> t[] = TSBHashTableDA.this.tabla;
                int s[] = TSBHashTableDA.this.states;

                if (current_entry >= t.length) {
                    return false;
                }

                int next_entry = current_entry + 1;
                for (int i = next_entry; i < t.length; i++) {
                    if (s[i] == 1) return true;
                }

                return false;
            }

            @Override
            public K next() {
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                Entry<K, V> t[] = TSBHashTableDA.this.tabla;
                int s[] = TSBHashTableDA.this.states;

                int next_entry = current_entry;
                for (next_entry++; s[next_entry] != 1; next_entry++) ;

                last_entry = current_entry;
                current_entry = next_entry;

                next_ok = true;

                K key = t[current_entry].getKey();

                return key;
            }

            @Override
            public void remove() {
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("remove(): modificación inesperada de tabla...");
                }

                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                TSBHashTableDA.this.tabla[current_entry] = null;
                TSBHashTableDA.this.states[current_entry] = 2;

                current_entry = last_entry;

                next_ok = false;

                TSBHashTableDA.this.count--;

                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    // Clase Privada EntrySet
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Map.Entry<?, ?>)) {
                return false;
            }

            Entry<K, V> t[] = TSBHashTableDA.this.tabla;
            int s[] = TSBHashTableDA.this.states;

            Entry<K, V> entry = (Entry<K, V>) o;

            int ih = TSBHashTableDA.this.h(entry.getKey());
            int ic = ih;
            int j = 1;

            while (s[ic] != 0) {
                if (s[ic] == 1) {
                    Entry<K, V> entryTable = t[ic];

                    if (entryTable.equals(entry)) return true;
                }

                ic += j * j;
                j++;
                if (ic >= t.length) {
                    ic %= t.length;
                }
            }

            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (o == null) {
                throw new NullPointerException("remove(): parámetro null");
            }
            if (!(o instanceof Map.Entry<?, ?>)) {
                return false;
            }

            Entry<K, V> t[] = TSBHashTableDA.this.tabla;
            int s[] = TSBHashTableDA.this.states;

            Entry<K, V> entry = (Entry<K, V>) o;

            int ih = TSBHashTableDA.this.h(entry.getKey());
            int ic = ih;
            int j = 1;

            while (s[ic] != 0) {

                if (s[ic] == 1) {
                    Entry<K, V> entryTable = t[ic];

                    if (entryTable.equals(entry)) {
                        t[ic] = null;
                        s[ic] = 2;

                        TSBHashTableDA.this.count--;
                        TSBHashTableDA.this.modCount++;

                        return true;
                    }
                }

                ic += j * j;
                j++;
                if (ic >= t.length) {
                    ic %= t.length;
                }
            }

            return false;
        }

        @Override
        public int size() {
            return TSBHashTableDA.this.count;
        }

        @Override
        public void clear() {
            TSBHashTableDA.this.clear();
        }

        private class EntrySetIterator implements Iterator<Map.Entry<K, V>> {
            private int last_entry;
            private int current_entry;
            private boolean next_ok;
            private int expected_modCount;

            public EntrySetIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            @Override
            public boolean hasNext() {
                Entry<K, V> t[] = TSBHashTableDA.this.tabla;
                int s[] = TSBHashTableDA.this.states;

                if (current_entry >= t.length) {
                    return false;
                }

                int next_entry = current_entry + 1;
                for (int i = next_entry; i < t.length; i++) {
                    if (s[i] == 1) return true;
                }

                return false;
            }

            @Override
            public Entry<K, V> next() {
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                Entry<K, V> t[] = TSBHashTableDA.this.tabla;
                int s[] = TSBHashTableDA.this.states;

                int next_entry = current_entry;
                for (next_entry++; s[next_entry] != 1; next_entry++) ;

                last_entry = current_entry;
                current_entry = next_entry;

                next_ok = true;

                return t[current_entry];
            }

            @Override
            public void remove() {
                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                TSBHashTableDA.this.tabla[current_entry] = null;
                TSBHashTableDA.this.states[current_entry] = 2;

                current_entry = last_entry;

                next_ok = false;

                TSBHashTableDA.this.count--;
                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }

    // Clase Privada ValueCollection
    private class ValueCollection extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new ValueCollectionIterator();
        }

        @Override
        public int size() {
            return TSBHashTableDA.this.count;
        }

        @Override
        public boolean contains(Object o) {
            return TSBHashTableDA.this.containsValue(o);
        }

        @Override
        public void clear() {
            TSBHashTableDA.this.clear();
        }

        private class ValueCollectionIterator implements Iterator<V> {
            private int last_entry;
            private int current_entry;
            private boolean next_ok;
            private int expected_modCount;

            public ValueCollectionIterator() {
                last_entry = 0;
                current_entry = -1;
                next_ok = false;
                expected_modCount = TSBHashTableDA.this.modCount;
            }

            @Override
            public boolean hasNext() {
                Entry<K, V> t[] = TSBHashTableDA.this.tabla;
                int s[] = TSBHashTableDA.this.states;

                if (current_entry >= t.length) {
                    return false;
                }

                int next_entry = current_entry + 1;
                for (int i = next_entry; i < t.length; i++) {
                    if (s[i] == 1) return true;
                }
                return false;
            }

            @Override
            public V next() {
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("next(): modificación inesperada de tabla...");
                }

                if (!hasNext()) {
                    throw new NoSuchElementException("next(): no existe el elemento pedido...");
                }

                Entry<K, V> t[] = TSBHashTableDA.this.tabla;
                int s[] = TSBHashTableDA.this.states;

                int next_entry = current_entry;
                for (next_entry++; s[next_entry] != 1; next_entry++) ;

                last_entry = current_entry;
                current_entry = next_entry;

                next_ok = true;

                V value = t[current_entry].getValue();

                return value;
            }

            @Override
            public void remove() {
                if (TSBHashTableDA.this.modCount != expected_modCount) {
                    throw new ConcurrentModificationException("remove(): modificación inesperada de tabla...");
                }

                if (!next_ok) {
                    throw new IllegalStateException("remove(): debe invocar a next() antes de remove()...");
                }

                TSBHashTableDA.this.tabla[current_entry] = null;
                TSBHashTableDA.this.states[current_entry] = 2;

                current_entry = last_entry;

                next_ok = false;

                TSBHashTableDA.this.count--;

                TSBHashTableDA.this.modCount++;
                expected_modCount++;
            }
        }
    }
}
