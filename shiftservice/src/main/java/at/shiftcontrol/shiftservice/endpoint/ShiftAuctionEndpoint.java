package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.shiftservice.dto.AuctionDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/auctions/{auctionId}/")
@RequiredArgsConstructor
public class ShiftAuctionEndpoint {
    // create endpoint is located in ShiftEndpoint

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getAuctionById",
        description = "Get auction by id"
    )
    public AuctionDto getAuctionById(@PathVariable String auctionId) {
        return null; // TODO: implement
    }

    @PostMapping("/claim")
    // TODO Security
    @Operation(
        operationId = "claimAuction",
        description = "Claim an auctioned shift"
    )
    public AuctionDto claimAuction(@PathVariable String auctionId) {
        return null; // TODO: implement
    }

    @DeleteMapping()
    // TODO Security
    @Operation(
        operationId = "cancelAuction",
        description = "Cancel an active auction (shift owner only)"
    )
    public void cancelAuction(@PathVariable String auctionId) {
        // TODO: implement 
    }
}
