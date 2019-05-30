$(function() {
  if (pointX !== undefined) {
    var s = Snap("#office_div");
    Snap.load("/static/office_plan.svg", function(data) {
      s.append(data);
      var snap = Snap("#office_map");
      var circle = snap.circle(pointX, pointY, 300);
      circle.attr({
        fill: "#ff0000",
        stroke: "#000",
        strokeWidth: 5
      });
      if (circle0X) {
        var circle = snap.circle(circle0X, circle0Y, circle0R);
        circle.attr({
          stroke: "#000",
          strokeWidth: 30,
          fill: "none"
        });
      }
      if (circle1X) {
        var circle = snap.circle(circle1X, circle1Y, circle1R);
        circle.attr({
          stroke: "#000",
          strokeWidth: 30,
          fill: "none"
        });
      }
      if (circle2X) {
        var circle = snap.circle(circle2X, circle2Y, circle2R);
        circle.attr({
          stroke: "#000",
          strokeWidth: 30,
          fill: "none"
        });
      }
    });
  }
});
