import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    private static ArrayList<String> savedGames = new ArrayList<>();
    private static boolean installedSuccessfully = true;
    private static final String gamesPath  = System.getProperty("user.dir") + "/Games/";

    public static void main(String[] args) {

        if (Install()) {
            System.out.println("Игра успешно установлена.");

            GameProgress start = new GameProgress(100, 200, 1, 1.7);
            GameProgress middle = new GameProgress(76, 105, 4, 24.2);
            GameProgress almostEnd = new GameProgress(20, 12, 12, 50.1);

            saveGame(gamesPath + "savegames/save.dat", start);
            saveGame(gamesPath + "savegames/save1.dat", middle);
            saveGame(gamesPath + "savegames/save2.dat", almostEnd);

            if (zipFiles(gamesPath + "savegames/zip.zip", savedGames)) {
                System.out.println("Сохранённые игры успешно заархивированы");
                savedGames.clear(); // Ну это так, на будущее, туда новые пути будут складываться :))
            }

            if (openZip(gamesPath + "savegames/zip.zip", gamesPath + "savegames/")) {
                new File(gamesPath + "savegames/zip.zip").delete();
                System.out.println("Сохранённые игры успешно разархивированы");
            }

            GameProgress loadedGame = openProgress(gamesPath + "savegames/save1.dat");

            if (loadedGame != null) {
                System.out.println("Сохранённая игра успешно загружена -> " + loadedGame);
            }

        } else {
            System.out.println("При установке игры возникла ошибка!");
        }

    }

    private static GameProgress openProgress(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (GameProgress) ois.readObject();
        } catch (Exception e) {
            System.out.println("Ошибка открытия прогресса - " + e.getMessage());
            return null;
        }
    }

    private static boolean openZip(String archivePath, String destinationPath) {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(archivePath))) {
            ZipEntry entry;
            String fileName;

            while ((entry = zin.getNextEntry()) != null) {
                fileName = entry.getName();
                FileOutputStream fout = new FileOutputStream(destinationPath + fileName);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                fout.flush();
                zin.closeEntry();
                fout.close();
            }

            return true;
        } catch (Exception e) {
            System.out.println("Ошибка распаковки архива - " + e.getMessage());
            return false;
        }
    }

    private static boolean zipFiles(String archivePath, ArrayList<String> savedGame) {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(archivePath))) {
            for (String saveFile : savedGame) {
                ZipEntry entry = new ZipEntry(new File(saveFile).getName());
                zout.putNextEntry(entry);

                FileInputStream fis = new FileInputStream(saveFile);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                zout.write(buffer);
                zout.closeEntry();
                fis.close();
                new File(saveFile).delete();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка создания архива - " + e.getMessage());
            return false;
        }
    }

    private static void saveGame(String path, GameProgress gameProgress) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(gameProgress);
            savedGames.add(path);
            System.out.println("Игра сохранена");
        } catch (Exception e) {
            System.out.println("Ошибка сохранения - " + e.getMessage());
        }
    }

    private static boolean Install() {
        if (!new File(gamesPath).exists()) {
            new File(gamesPath).mkdir();
        }

        StringBuilder log = new StringBuilder();

        log.append(createFolder(gamesPath + "src"));
        log.append(createFolder(gamesPath + "res"));
        log.append(createFolder(gamesPath + "savegames"));
        log.append(createFolder(gamesPath + "temp"));
        log.append(createFolder(gamesPath + "src/main"));
        log.append(createFolder(gamesPath + "src/test"));
        log.append(createFile(gamesPath + "src/main/Main.java"));
        log.append(createFile(gamesPath + "src/main/Utils.java"));
        log.append(createFolder(gamesPath + "res/drawables"));
        log.append(createFolder(gamesPath + "res/vectors"));
        log.append(createFolder(gamesPath + "res/icons"));
        log.append(createFile(gamesPath + "temp/temp.txt"));

        try (FileWriter w = new FileWriter(gamesPath + "temp/temp.txt", false)) {
            w.write(log.toString());
        } catch (IOException e) {
            System.out.println("Ошибка записи лога установки - " + e.getMessage());
        }

        return installedSuccessfully;
    }

    private static String createFolder (String path) {
        if (new File(path).mkdir()) {
            return "Директория " + path + " успешно создана\n";
        }

        if (!new File(path).exists()) {
            installedSuccessfully = false;
            return "При создании директории " + path + " произошла ОШИБКА!\n";
        } else {
            return "Директория " + path + " уже существует!\n";
        }
    }

    private static String createFile (String path) {
        try {
            new File(path).createNewFile();
            return "Файл " + path + " успешно создан\n";
        } catch (IOException e) {
            installedSuccessfully = false;
            return "При создании файла " + path + " произошла ОШИБКА! (" + e.getMessage() + ")\n";
        }
    }

}
