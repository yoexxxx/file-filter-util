## FILE FILTER UTIL

---

### Версия Java:
* 21

### Система сборки:
* Maven 3.9.2

### Используемые библиотеки:
#### Lombok
* **Версия:** `1.18.38`
* **Назначение на проекте:** Уменьшение шаблонного кода, делает структуру более читаемой и лаконичной
* **Maven зависимость:**
    ```xml
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.38</version>
      <scope>provided</scope>
    </dependency>
    ```
---

#### Picocli
* **Версия:** `4.7.7`
* **Назначение на проекте:** Упрощение парсинга аргументов (опций) командной строки
* **Maven зависимость:**
    ```xml
    <dependency>
      <groupId>info.picocli</groupId>
      <artifactId>picocli</artifactId>
      <version>4.7.7</version>
    </dependency>
    ```
---

#### Logback
* **Версия:** `1.5.18`
* **Назначение на проекте:** Реализация логирования
* **Maven зависимость:**
    ```xml
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.5.18</version>
    </dependency>
    ```
---

#### Apache Commons Libs
* **Назначение на проекте:** Для использования проверенных утилит и упрощения при работе с классами из java.lang, коллекциями, файлами и т.д.
* **Maven зависимость:**
    ```xml
  <dependencies>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.18.0</version>
    </dependency>
     <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-collections4</artifactId>
      <version>4.5.0</version>
     </dependency>
      <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.20.0</version>
        <scope>test</scope>
      </dependency>
   </dependencies>
    ```

---

#### Junit
* **Версия:** `5.13.4`
* **Назначение на проекте:** Написание теста, для проверки работы утилиты
* **Maven зависимость:**
    ```xml
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.13.4</version>
      <scope>test</scope>
    </dependency>
    ```
---

### Общее описание:
Утилита принимает на вход один или несколько текстовых файлов (.txt), в которых строки могут содержать целые числа, вещественные числа или же текст. Каждую строку программа анализирует, разделяет по типам данных, а далее записывает:
- целые числа в файл `integers.txt`
- вещественные – в файл `floats.txt`
- все остальные строки (не являющиеся числом) – в файл `strings.txt`

По умолчанию файлы создаются в текущей директории. Если какого-то типа данных нет во входных файлах - пустой файл не создаётся (либо удаляется, если оказался пустым и не установлен параметр append).
Утилита собирает статистику по обработанным данным и выводит её на консоль.

### Опции:
Для полноты работы утилиты, пользователь при запуске может указать опции, которые способны изменить её поведение по умолчанию. Ниже приведены поддерживаемые опции:

- Опция `-o <path>` задаёт каталог для результатов (по умолчанию – текущая директория).
- Опция `-p <prefix>` добавляет префикс к именам файлов (например, префикс `result_` превратит `integers.txt` в `result_integers.txt`).
- Опция `-a` включает режим добавления в существующие файлы (append). По умолчанию файлы перезаписываются.

Поддерживается два режима вывода статистики:
- **Краткая статистика (`-s`)** – выводит только количество записанных элементов каждого типа данных.
- **Полная статистика (`-f`)** – для чисел опция предусматривает вывод минимального/максимального значения, их сумму, а также среднее значение; для строк – минимальную/максимальную длину строк.

Если опции статистики не были заданы, то программа лишь предупреждает об отсутствии заданных параметров для вывода статистики, а сама статистика не выводится.

---

### Особенности технической реализации:
В реализации используется многопоточная архитектура с применением виртуальных потоков доступных в Java 21.

Каждый переданный с командной строки файл (.txt) построчно читается отдельным потоком-читателем [FileToQueueReader](src/main/java/org/filimonov/file/filter/util/io/reader/FileToQueueReader.java),
это обеспечивает высокую скорость при обработке большого числа файлов.
Поток-читатель парсит строку при помощи `NumberUtils` из Apache Commons Lang и помещает результат в соответствующую полученному типу внутреннюю очередь в [ReaderQueue](src/main/java/org/filimonov/file/filter/util/queue/ReaderQueue.java). 
При завершении чтения файла, он помечает свой [ReaderQueue](src/main/java/org/filimonov/file/filter/util/queue/ReaderQueue.java) как завершенный и заканчивает свою работу.

В это время [Coordinator](src/main/java/org/filimonov/file/filter/util/queue/Coordinator.java), который отвечает за координацию работы читателей собирает данные из внутренних очередей читателей [ReaderQueue](src/main/java/org/filimonov/file/filter/util/queue/ReaderQueue.java) 
и помещает их в соответствующие типам общие очереди [CommonQueue](src/main/java/org/filimonov/file/filter/util/queue/CommonQueue.java).
**Координатор по очереди перебирает [ReaderQueue](src/main/java/org/filimonov/file/filter/util/queue/ReaderQueue.java) каждого читателя, чтобы сохранить порядок в выходных файлах, так как это важно в нашем задании**
и после того, как видит, что [ReaderQueue](src/main/java/org/filimonov/file/filter/util/queue/ReaderQueue.java) помечен как завершенный, то переходит к следующему, который как раз уже успел вычитать какое-то количество строк 
(максимальный размер задан до 1000 для очереди каждого типа, чтобы не потратить всю память при больших файлах).
После того как все [ReaderQueue](src/main/java/org/filimonov/file/filter/util/queue/ReaderQueue.java) будут завершены, координатор помечает что все читатели завершили свою работу.

После того как данные попали в [CommonQueue](src/main/java/org/filimonov/file/filter/util/queue/CommonQueue.java) три потока-писателя в зависимости от типа данных
([StringToFileWriter](src/main/java/org/filimonov/file/filter/util/io/writer/StringToFileWriter.java),
[LongToFileWriter](src/main/java/org/filimonov/file/filter/util/io/writer/LongToFileWriter.java),
[DoubleToFileWriter](src/main/java/org/filimonov/file/filter/util/io/writer/DoubleToFileWriter.java))
берут данные из общих очередей и записывают их в соответствующие файлы до тех пор, пока очередь не пуста и все читатели не завершили свою работу.

Созданием файлов для записи результатов занимается отдельный объект [FileManager](src/main/java/org/filimonov/file/filter/util/manager/FileManager.java),
который получает от парсера командной строки входные параметры касаемо файлов (`-o <path>`, `-p <prefix>`) исходя из которых он формирует файлы для результатов.

Сбор статистики выполняет [StatsManager](src/main/java/org/filimonov/file/filter/util/manager/StatsManager.java), который в момент записи данных в файлы обновляет счётчики значений.
[StatsManager](src/main/java/org/filimonov/file/filter/util/manager/StatsManager.java) атомарно учитывает количество, сумму, минимумы и максимумы по каждому типу данных, а использование атомарных переменных - позволяет безопасно работать в многопоточной среде. 
После завершения обработки вызывается метод `printStats()` , который выводит на консоль статистику согласно выбранному режиму (`-s` или `-f`).

Все операции чтения и записи обёрнуты в блоки `try-catch`. В случае возникновения ошибок с конкретным файлом или строкой программа логирует ошибку и продолжает работу с остальными данными.
По задумке непредвиденное исключение не приводит к прерыванию работы утилиты – вместо этого в логах можно увидеть сообщение о той или иной возникшей ошибке.

Также подготовлен тест, для проверки работоспособности утилиты - [AppTest](src/test/java/org/filimonov/file/filter/util/AppTest.java)

---

### Пример работы утилиты:

В проекте есть два файла для тестового запуска, а именно [in1.txt](src/main/resources/files/in1.txt) и [in2.txt](src/main/resources/files/in2.txt)

**`in1.txt` содержит:**
```
Lorem ipsum dolor sit amet
45
Пример
3.1415
consectetur adipiscing
-0.001
тестовое задание
100500
```

**`in2.txt` содержит:**
```
Нормальная форма числа с плавающей запятой
1.528535047E-25
Long
1234567890123456789
```

**Пример запуска программы:**

Чтобы получить краткую статистику и добавить префикс sample- к результатам в режиме дописывания, а результаты находились в директории `src/main/resources/files/result`, нужно выполнить следующее:
```bash
java -jar target/file-filter-util-1.0-SNAPSHOT.jar -s -a -p sample- -o src/main/resources/files/result  src/main/resources/files/in1.txt  src/main/resources/files/in2.txt
```

В этом случае будут созданы (или дополнены) файлы:

- sample-integers.txt – с целыми числами, содержащий:
```
  45
  100500
  1234567890123456789
 ```
- sample-floats.txt – с вещественными числами, содержащий:
```
  3.1415
  -0.001
  1.528535047E-25
 ```
- sample-strings.txt – со строками, содержащий:
```
  Lorem ipsum dolor sit amet
  Пример
  consectetur adipiscing
  тестовое задание
  Нормальная форма числа с плавающей запятой
  Long
```

В консоли можно будет увидеть следующее:
```
15:39:37.614 [main] INFO  org.filimonov.file.filter.util.App - Program started successfully!
15:39:37.698 [virtual-23] INFO  o.f.f.f.u.i.reader.FileToQueueReader - Started reading file: [in1.txt]
15:39:37.698 [virtual-25] INFO  o.f.f.f.u.i.reader.FileToQueueReader - Started reading file: [in2.txt]
15:39:37.731 [virtual-25] INFO  o.f.f.f.u.i.reader.FileToQueueReader - File [src\main\resources\files\in2.txt] was successfully read, total: Long - [1], Double - [1], String - [2]
15:39:37.732 [virtual-23] INFO  o.f.f.f.u.i.reader.FileToQueueReader - File [src\main\resources\files\in1.txt] was successfully read, total: Long - [2], Double - [2], String - [4]
15:39:37.733 [virtual-35] INFO  o.f.f.f.u.i.writer.QueueToFileWriter - File [src\main\resources\files\result\sample-floats.txt] was written successfully
15:39:37.843 [virtual-30] INFO  o.f.f.f.u.i.writer.QueueToFileWriter - File [src\main\resources\files\result\sample-strings.txt] was written successfully
15:39:37.843 [virtual-32] INFO  o.f.f.f.u.i.writer.QueueToFileWriter - File [src\main\resources\files\result\sample-integers.txt] was written successfully
Long total count -->> 3
Double total count -->> 3
String total count -->> 6
15:39:37.844 [main] INFO  org.filimonov.file.filter.util.App - Program terminated!
```