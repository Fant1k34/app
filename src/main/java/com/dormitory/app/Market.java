package com.dormitory.app;

import com.dormitory.app.database.Business;
import com.dormitory.app.database.InsertData;
import com.dormitory.app.database.SetConnection;
import com.dormitory.app.helpful.FindProperly;
import com.dormitory.app.helpful.MarketNewsCreator;
import com.dormitory.app.helpful.PictureMarket;
import com.dormitory.app.helpful.alhoritms.forMarketClass;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;

@Controller
public class Market {
    @RequestMapping(value = "/market", method = RequestMethod.GET)
    public String marketPage(Model model, @RequestParam(value = "sort", defaultValue = "rating") String sort,
                             HttpSession session){
        try {
            if (session.getAttribute("login") == null) {
                session.setAttribute("isLikeButtonActive", false);
                session.setAttribute("search", "");
                // model.addAttribute("search", new FindProperly(""));
                return "redirect:/";
            }
            String login = (String) session.getAttribute("login");
            boolean liked = (boolean) session.getAttribute("isLikeButtonActive");

            if (sort.equals("date")) {
                model.addAttribute("marketNews", putAllNewsToModel(SortFlagForMarket.DATE, login, liked, (String) session.getAttribute("search")));
            }
            if (sort.equals("name")) {
                model.addAttribute("marketNews", putAllNewsToModel(SortFlagForMarket.NAME, login, liked, (String) session.getAttribute("search")));
            }
            if (sort.equals("rating")) {
                model.addAttribute("marketNews", putAllNewsToModel(SortFlagForMarket.RATING, login, liked, (String) session.getAttribute("search")));
            }

            model.addAttribute("sortType", sort);

            if (liked) {
                model.addAttribute("isLikeButtonActive", true);
            } else {
                model.addAttribute("isLikeButtonActive", false);
            }

            if (session.getAttribute("contactInfo") == null) {
                model.addAttribute("contactInfo", "");
                model.addAttribute("blockId", "");
            } else {
                String s1 = (String) session.getAttribute("contactInfo");
                String s2 = (String) session.getAttribute("blockId");
                System.out.println(s1);
                System.out.println(s2);
                model.addAttribute("contactInfo", s1);
                model.addAttribute("blockId", s2);
                // Обнуляем данные два аттрибута сессии
                session.setAttribute("contactInfo", null);
                session.setAttribute("blockId", null);

            }

            model.addAttribute("group_id", (int) session.getAttribute("group_id"));
            model.addAttribute("login", (String) session.getAttribute("login"));
            model.addAttribute("prevSearch", (String) session.getAttribute("search"));
            model.addAttribute("search", new FindProperly());

            if ((int) session.getAttribute("group_id") == 2){
                session.setAttribute("exception", "Получите МЕГА-версию, чтобы не ждать");
                model.addAttribute("exception", (String) session.getAttribute("exception"));
                session.setAttribute("exception", null);
            }
            return "market";
        }
        catch (Exception e){
        }
        return "redirect:/";
    }

    public static ArrayList<MarketNewsCreator> putAllNewsToModel(SortFlagForMarket sortFlagForMarket, String login, boolean sortByLiked, String word){
        ArrayList<MarketNewsCreator> marketNewsCreatorArrayList = Business.putAllNewsToMarketNewsCreator(sortFlagForMarket, login);

        for (MarketNewsCreator marketNewsCreator : marketNewsCreatorArrayList) {
            ArrayList<PictureMarket> allPicturesFromDBById = Business.getAllPicturesFromDBById(Integer.parseInt(marketNewsCreator.getMark_id()));
            try {
                marketNewsCreator.setImageId(String.valueOf(allPicturesFromDBById.get(0).getIdPicture()));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }


        Collections.sort(marketNewsCreatorArrayList);

        ArrayList<MarketNewsCreator> answerForSortByLikeAndSearch = new ArrayList<>();

        if (sortByLiked) {
            marketNewsCreatorArrayList.stream().filter(MarketNewsCreator::isLiked).filter(i -> forMarketClass.filterBySearch(i, word)).forEach(answerForSortByLikeAndSearch::add);
        }
        else {
            marketNewsCreatorArrayList.stream().filter(i -> forMarketClass.filterBySearch(i, word)).forEach(answerForSortByLikeAndSearch::add);
        }

        return answerForSortByLikeAndSearch;
    }

    @RequestMapping("/market/liked")
    public String likeToDD(@RequestParam("newid") String id, HttpSession session, Model model){
        try {
            Connection connection = SetConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Liked AS L WHERE new_mar_id = ? AND L.user_id = (SELECT user_id from User WHERE login = ?)");
            statement.setInt(1, Integer.parseInt(id));
            statement.setString(2, (String) session.getAttribute("login"));
            ResultSet resultSet = statement.executeQuery();
            int counterLiked = 0;
            while (resultSet.next()){
                counterLiked++;
            }
            if (counterLiked > 1){
                throw new RuntimeException("Несколько ваших лайков на одном комментарии!");
            }

            // Уже лайк есть. Значит, надо удалить его!
            if (counterLiked == 1) {
                PreparedStatement statement2 = connection.prepareStatement("SELECT user_id from User WHERE login = ?");
                statement2.setString(1, (String) session.getAttribute("login"));
                ResultSet resultSet2 = statement2.executeQuery();
                resultSet2.next();
                int answerForSubQuery = resultSet2.getInt(1);
                statement2.close();

                PreparedStatement statement1 = connection.prepareStatement("DELETE FROM Liked WHERE new_mar_id = ? AND user_id = ?");
                statement1.setInt(1, Integer.parseInt(id));
                statement1.setInt(2, answerForSubQuery);
                statement1.execute();
                statement1.close();
                return "redirect:/market";
            }

            PreparedStatement statement1 = connection.prepareStatement("INSERT INTO Liked VALUES ((SELECT user_id FROM User WHERE login = ?), ?)");
            statement1.setString(1, (String) session.getAttribute("login"));
            statement1.setInt(2, Integer.parseInt(id));
            statement1.execute();


            statement.close();
            connection.close();
        }
        catch (Exception e)
            {e.printStackTrace();}

        return "redirect:/market";
    }

    @RequestMapping("/sortByLiked")
    public String sortByLikedMarketNews(HttpSession session){
        boolean liked = (boolean) session.getAttribute("isLikeButtonActive");
        session.setAttribute("isLikeButtonActive", !liked);
        return "redirect:/market";
    }


    @RequestMapping(value = "/market", method = RequestMethod.POST)
    public String marketPagePost(Model model, @RequestParam(value = "sort", defaultValue = "rating") String sort,
                             HttpSession session, @ModelAttribute("search") FindProperly findProperly) {
        System.out.println(findProperly.getSubsequence());
        session.setAttribute("search", findProperly.getSubsequence());
        return "redirect:/market";
    }
}
