# Solution Report

My implementation takes in a `BidRequest` via the http server and sends it to the `CampaignRegistry` actor which processes it. The processing is done by the `BidProcessor` by filtering through the active campaigns available to find matching campaigns. The filtering is done based on the following criteria:  

## Bid Floor Filter

The bid floor is the minimum acceptable price for a valid bid. This filter finds acceptable campaigns by checking the impressions in the request for their bid floors and matching them against a campaign to see if it finds the bid acceptable

## Country Filter

The country filter checks through the provided user and device objects for their target countries and attempts to match them against campaign by their country. It's important to note that the device country takes priority over the user country.

## Targeting Filter

The targeting filter checks if the site id is valid for the request

## Banner Filter

This filter checks if the banner for the campaign is a match for the request by checking via provided width or mininum width / maximum width (Same thing for height) values

The filters are gone through in a certain order to ensure that the campaign list is thinned as much as possible on each filter. This is just an optimization to make the process as fast as possible i.e the filters can be run in any order.

After a matching campaign has been found, it is passed to the `BidResponseBuilder` to parse it into a `BidResponse` and then it's sent to the user.

If no matching campaign is found the server returns a  

```http
  204 No Content Response
```
