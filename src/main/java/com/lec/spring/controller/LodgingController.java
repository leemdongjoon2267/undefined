package com.lec.spring.controller;


import com.lec.spring.domain.*;
import com.lec.spring.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lodging")
public class LodgingController {
    private final LodgingService lodgingService;
    private ProviderService providerService;
    private RoomService roomService;
    private final PostService postService;

    private BookingService bookingService;

    @Autowired
    public LodgingController(LodgingService lodgingService, ProviderService providerService, RoomService roomService, PostService postService) {
        this.lodgingService = lodgingService;
        this.providerService = providerService;
        this.roomService = roomService;
        this.postService = postService;
    }

    @GetMapping("/LodgingSearch")
    public String showLodgingSearch() {
        return "lodging/LodgingSearch";
    }

    @GetMapping("/LodgingList")
    public void showLodgingListPage() {
    }

    @PostMapping("/LodgingList")
    public String handleSearchRequest(@RequestParam("location") String location, Model model) {
        List<Lodging> lodgings = lodgingService.getLodgingsByLocation(location);

        // 각 숙소에 대해 평균 점수를 설정
        for (Lodging lodging : lodgings) {
            Double avgPostGrade = lodgingService.getAvgPostGrade(lodging.getLodgingId());
            Integer totalPosts = lodgingService.getTotalPosts(lodging.getLodgingId());
            lodging.setAvgPostGrade(avgPostGrade != null ? avgPostGrade : 0.0);
            lodging.setTotalPosts(totalPosts != null ? totalPosts : 0);
        }

        model.addAttribute("lodging", lodgings);
        model.addAttribute("location", location); // location 정보를 모델에 추가
        return "lodging/LodgingList";
    }

    @PostMapping("/LodgingList/filter")
    public String filterLodgings(@RequestParam("location") String location, @RequestParam("type") String type, Model model) {
        List<Lodging> lodgings = lodgingService.getLodgingsByLocationAndType(location, type);
        for (Lodging lodging : lodgings) {
            Double avgPostGrade = lodgingService.getAvgPostGrade(lodging.getLodgingId());
            Integer totalPosts = lodgingService.getTotalPosts(lodging.getLodgingId());
            lodging.setAvgPostGrade(avgPostGrade != null ? avgPostGrade : 0.0);
            lodging.setTotalPosts(totalPosts != null ? totalPosts : 0);
        }
        model.addAttribute("lodging", lodgings);
        return "lodging/LodgingList :: #item-list"; // Thymeleaf fragment
    }

    @PostMapping("/LodgingList/price")
    public String filterPrice(@RequestParam("location") String location,
                              @RequestParam("price") String price,
                              Model model) {
        List<Lodging> lodgings;
        if (price.equals("DESC")) {
            lodgings = lodgingService.findLodgingByPriceDESC(location);
        } else {
            lodgings = lodgingService.findLodgingByPriceASC(location);
        }
        addAdditionalInfoToLodgings(lodgings);
        model.addAttribute("lodging", lodgings); // 'lodgings'를 'lodging'으로 변경
        return "lodging/LodgingList :: #item-list";
    }
    // 추가 정보를 설정하는 메서드
    private void addAdditionalInfoToLodgings(List<Lodging> lodgings) {
        for (Lodging lodging : lodgings) {
            Double avgPostGrade = lodgingService.getAvgPostGrade(lodging.getLodgingId());
            Integer totalPosts = lodgingService.getTotalPosts(lodging.getLodgingId());
            lodging.setAvgPostGrade(avgPostGrade != null ? avgPostGrade : 0.0);
            lodging.setTotalPosts(totalPosts != null ? totalPosts : 0);
            // 필요한 경우 추가 정보 설정
        }
    }


    
    @GetMapping("/LodgingDetail/{lodgingId}")
    public String getLodgingDetail(@PathVariable("lodgingId") Long lodgingId, Model model) {
        List<Lodging> lodgings = lodgingService.lodgingDetail(lodgingId);
        List<Lodging> lodgingName = lodgingService.lodgingName(lodgingId);
        List<Post> lodgingPost = postService.findPostByLodgingId(lodgingId);
        for (Lodging lodging : lodgings) {
            Double avgPostGrade = lodgingService.getAvgPostGrade(lodging.getLodgingId());
            Integer totalPosts = lodgingService.getTotalPosts(lodging.getLodgingId());
            lodging.setAvgPostGrade(avgPostGrade != null ? avgPostGrade : 0.0);
            lodging.setTotalPosts(totalPosts != null ? totalPosts : 0);
        }


        model.addAttribute("lodging", lodgings);
        model.addAttribute("lodgingName", lodgingName);
        model.addAttribute("lodgingPost", lodgingPost);

        return "lodging/LodgingDetail";
    }

    @GetMapping("/LodgingPostList/{lodgingId}")
    public String postList (@PathVariable("lodgingId") Long lodgingId, Model model) {
        List<Lodging> lodgingss = lodgingService.getPostList(lodgingId);
        Double avgPostGrade = lodgingService.getAvgPostGrade(lodgingId);
        int totalPosts = postService.countAllPostsByLodgingId((long)lodgingId);

        String formattedAvgPostGrade = String.format("%.1f", avgPostGrade);
        model.addAttribute("lodgingss", lodgingss);
        model.addAttribute("avgPostGrade", formattedAvgPostGrade);
        model.addAttribute("totalPosts", totalPosts);
        return "lodging/LodgingPostList";
    }

    @GetMapping("/RoomDetail/{lodgingId}/{roomId}")
    public String RoomDetail(@PathVariable("lodgingId") Long lodgingId, @PathVariable("roomId") Long roomId, Model model) {
        ProvLodging lodging = providerService.getAllDetails(lodgingId);
        Room room = roomService.findByRoomId(roomId);
        List<Post> postList = postService.findPostByLodgingId(lodgingId);
        int totalPosts = postService.countAllPostsByLodgingId((long)lodgingId);

        if(postList.size() > 3)
            postList = postList.subList(0, 3);

        model.addAttribute("lodging", lodging);
        model.addAttribute("room", room);
        model.addAttribute("roomPrice", DecimalFormat.getInstance().format(room.getRoomPrice()));
        model.addAttribute("postList", postList);
        model.addAttribute("totalPosts", totalPosts);

        return "lodging/RoomDetail";
    }


}
