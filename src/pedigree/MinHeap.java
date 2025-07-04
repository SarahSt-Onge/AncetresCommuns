package pedigree;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Classe MinHeap générique qui implémente un tas minimum.
 * Utilise un comparateur pour déterminer l'ordre des éléments.
 */
class MinHeap<T> {
    private final ArrayList<T> heap = new ArrayList<>();
    private final Comparator<T> comparator;

    /**
     * Constructeur avec comparateur personnalisé.
     */
    public MinHeap(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * Insère un élément dans le tas.
     */
    public void insert(T item) {
        heap.add(item);
        siftUp(heap.size() - 1);
    }

    /**
     * Extrait et retourne l'élément minimal.
     */
    public T extractMin() {
        if (heap.isEmpty()) return null;
        T min = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return min;
    }

    /**
     * Vérifie si le tas est vide.
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Retourne la taille du tas.
     */
    public int size() {
        return heap.size();
    }

    /**
     * Consulte l'élément minimal sans le retirer.
     */
    public T peekMin() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    /**
     * Maintient la propriété de tas en remontant un élément.
     */
    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(heap.get(index), heap.get(parent)) < 0) {
                swap(index, parent);
                index = parent;
            } else break;
        }
    }

    /**
     * Maintient la propriété de tas en descendant un élément.
     */
    private void siftDown(int index) {
        int size = heap.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size && comparator.compare(heap.get(left), heap.get(smallest)) < 0)
                smallest = left;
            if (right < size && comparator.compare(heap.get(right), heap.get(smallest)) < 0)
                smallest = right;

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else break;
        }
    }

    /**
     * Échange deux éléments dans le tas.
     */
    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}