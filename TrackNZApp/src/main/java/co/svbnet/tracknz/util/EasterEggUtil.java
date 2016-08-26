package co.svbnet.tracknz.util;

import java.util.Random;

/**
 * Ssshhhh...........
 */
public class EasterEggUtil {

    private static String[] fortunes = {
        "Look to La Luna",
        "Don't leave the house today",
        "We will all die one day",
        "Ask again later",
        "Wake up",
        "You are worshiping a sun god",
        "Stay asleep",
        "Marry and reproduce",
        "Question authority",
        "Think for yourself",
        "Steven lives",
        "Bring him the photo",
        "Your soul is hidden deep within the darkness",
        "You are dark inside",
        "When life gives you lemons, reroll!",
        "It is dangerous to go alone",
        "Go to the next room",
        "Why so blue?",
        "Your princess is in another castle",
        "You make mistakes, it is only natural",
        "A hanged man brings you no luck today",
        "The devil in disguise",
        "Nobody knows the troubles you have seen",
        "Always your head in the clouds",
        "Do not lose your head",
        "Do not cry over spilled tears",
        "Well that was worthless",
        "Sunrays on your little face",
        "Have you seen the exit?",
        "Always look on the bright side",
        "Get a baby pet, it will cheer you up",
        "Meet strangers without prejudice",
        "Only a sinner",
        "See what he sees, do what he does",
        "Lies",
        "Lucky numbers: 16 31 64 70 74",
        "Go directly to jail",
        "Rebirth got cancelled",
        "Follow the cat",
        "Take your medicine",
        "Come to a fork in the road, take it",
        "Believe in yourself",
        "Trust no one",
        "Trust good people",
        "Follow the dog",
        "Follow the zebra",
        "What do you want to do today",
        "Use bombs wisely",
        "You are playing it wrong, give me the controller",
        "Choose your own path",
        "I feel asleep!!!"
    };

    private static Random random = new Random();

    public static String newFortune() {
        return fortunes[random.nextInt(fortunes.length - 1)];
    }

}
