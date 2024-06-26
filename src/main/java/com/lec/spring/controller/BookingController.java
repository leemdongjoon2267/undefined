package com.lec.spring.controller;

import com.lec.spring.config.PrincipalDetails;
import com.lec.spring.domain.*;

import com.lec.spring.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BookingController {

    private BookingService bookingService;
    private LodgingService lodgingService;
    private RoomService roomService;
    private UserService userService;

    @Autowired
    public BookingController(
            BookingService bookingService
            , LodgingService lodgingService
            , RoomService roomService
            , UserService userService
    ) {
        this.bookingService = bookingService;
        this.lodgingService = lodgingService;
        this.roomService = roomService;
        this.userService = userService;
    }

    @GetMapping("/lodging/LodgingBooking")
    public String lodgingBooking(@RequestParam("lodgingId") Long lodgingId,
                                 @RequestParam(value = "roomId", required = false) Long roomId,
                                 Model model,
                                 Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            // 인증되지 않은 사용자 처리
            return "redirect:/login"; // 로그인 페이지로 리다이렉트 또는 예외 처리
        }

        Object principal = authentication.getPrincipal();
        User user = null;
        if (principal instanceof PrincipalDetails) {
            PrincipalDetails principalDetails = (PrincipalDetails) principal;
            user = principalDetails.getUser();
            model.addAttribute("user", user);
        } else if (principal instanceof String) {
            String username = (String) principal;
            user = userService.findByUsername(username);
            model.addAttribute("user", user);
        } else {
            // 다른 타입에 대한 처리
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }

        // 사용자 정보 모델에 추가
        model.addAttribute("phonenum", user.getPhonenum());

        Lodging lodging = lodgingService.getLodgingById(lodgingId);
        model.addAttribute("lodging", lodging);

        // Lodging ID에 해당하는 Room 목록 조회
        List<Room> rooms = roomService.findRoomsByLodgingId(lodgingId);
        model.addAttribute("rooms", rooms);

        // 선택한 객실 정보 조회
        Room selectedRoom = null;
        if (roomId != null && roomId != 0) {
            selectedRoom = roomService.getRoomById(roomId);
        }
        model.addAttribute("selectedRoom", selectedRoom);

        model.addAttribute("booking", new Booking());

        return "lodging/LodgingBooking";
    }

    @GetMapping("/mypage/provider/ProvBookingList/{userId}")
    public void provBookingList(Model model) {
    }

    @GetMapping("/mypage/customer/BookingList/{userId}")
    public String BookingList(@PathVariable("userId") Long userId, Model model){
        List<Booking> books = bookingService.findBooksByUserId(userId);
        List<Booking> booksBefore = new ArrayList<>();
        List<Booking> booksAfter = new ArrayList<>();
        books.forEach(book -> {
            book.setFormattedPay(DecimalFormat.getInstance().format(book.getBookingPay()));
            book.setDateGap(Period.between(book.getBookingStartDate(), book.getBookingEndDate()).getDays());
            if(Period.between(LocalDate.now(), book.getBookingEndDate()).getDays() >= 0) booksBefore.add(book);
            else booksAfter.add(book);
        });
        model.addAttribute("booksBefore", booksBefore);
        model.addAttribute("booksAfter", booksAfter);
        return "mypage/customer/BookingList";
    }

    @PostMapping("/mypage/customer/deleteBooking/{userId}/{bookingId}")
    public String deleteBooking(@PathVariable("userId") Long userId,
                                @PathVariable("bookingId") Long bookingId,
                                Model model) {

        LocalDate bookingStartDate = bookingService.getBookingStartDate(bookingId);
        if (!bookingStartDate.isAfter(LocalDate.now())) {
            model.addAttribute("errorMessage", "예약을 취소할  수 없습니당");
            return "error";
        }

        bookingService.deleteByBookingId(bookingId);
        return "redirect:/mypage/customer/BookingList/" + userId;
    }

}